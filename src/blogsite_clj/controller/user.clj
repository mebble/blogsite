(ns blogsite-clj.controller.user
  (:require
   [blogsite-clj.auth :refer [create-session]]
   [blogsite-clj.model.user :as m]
   [blogsite-clj.model.blog :as mb]
   [buddy.hashers :as hashers]
   [ring.util.response :refer [header]]
   [selmer.parser :refer [render-file]]))

(defn login-page []
  (render-file "views/login.html" {}))

(defn- -login [db req]
  (let [password (-> req :params :password)
        username (-> req :params :username)]
    (if-let [user (m/get-user db username)]
      (if (:valid (hashers/verify password (:password user)))
        (assoc
         (header {} "HX-Location" "/dashboard")
         :session (create-session user))
        {:body "Wrong password"})
      (header {} "HX-Location" "/login"))))

(defn- signup [db req]
  (let [user-in (-> (select-keys (:params req) [:username :password])
                    (update :password (fn [p] (hashers/derive p {:alg :bcrypt+blake2b-512}))))
        user (m/insert-user db user-in)]
    (assoc
     (header {} "HX-Location" "/dashboard")
     :session (create-session user))))

(defn login [db req]
  (case (get-in req [:params :type])
    "login" (-login db req)
    "signup" (signup db req)
    {:status 400}))

(defn user-page [db username]
  (when-let [user (m/get-user db username)]
    (let [blogs (mb/get-blogs-by-user db (:id user))]
      (render-file "views/user.html" {:user user :blogs blogs}))))

(defn dashboard [db req]
  (let [username (get-in req [:session :username])
        user (m/get-user db username)
        blogs (mb/get-blogs-by-user db (:id user))]
    (render-file "views/dashboard.html" {:user user :blogs blogs})))

(defn logout []
  (header {:session nil} "HX-Location" "/"))
