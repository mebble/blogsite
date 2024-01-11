(ns blogsite-clj.controller.user
  (:require
   [blogsite-clj.auth :refer [create-session]]
   [blogsite-clj.model.user :as m]
   [blogsite-clj.model.post :as mp]
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

(defn user-page [db req]
  (let [username (get-in req [:params :username])]
    (when-let [user (m/get-user db username)]
      (let [posts (mp/get-posts-by-user db (:id user))]
        (render-file "views/user.html" {:user user :posts posts :session (:session req)})))))

(defn dashboard [db req]
  (let [username (get-in req [:session :username])
        user (m/get-user db username)
        posts (mp/get-posts-by-user db (:id user))]
    (render-file "views/dashboard.html" {:user user :posts posts})))

(defn logout []
  (header {:session nil} "HX-Location" "/"))
