(ns blogsite-clj.controller.user
  (:require
   [blogsite-clj.model.user :as m]
   [buddy.hashers :as hashers]
   [ring.util.response :refer [header]]
   [selmer.parser :refer [render-file]]))

(defn- redirect-url [user]
  (str "/" (:username user)))

(defn login-page []
  (render-file "views/login.html" {}))

(defn- -login [db req]
  (let [password (-> req :params :password)
        username (-> req :params :username)
        user (m/get-user db username)
        hashed (:password user)]
    (if (:valid (hashers/verify password hashed))
      (assoc
       (header {} "HX-Location" (redirect-url user))
       :session (:username user))
      {:body "Wrong password"})))

(defn- signup [db req]
  (let [user-in (-> (select-keys (:params req) [:username :password])
                    (update :password (fn [p] (hashers/derive p {:alg :bcrypt+blake2b-512}))))
        user (m/insert-user db user-in)]
    (assoc
     (header {} "HX-Location" (redirect-url user))
     :session (:username user))))

(defn login [db req]
  (case (get-in req [:params :type])
    "login" (-login db req)
    "signup" (signup db req)
    {:status 400}))

(defn user-page [db username]
  (some-> (m/get-user db username)
          (#(render-file "views/user.html" {:user %1}))))

(defn dashboard [db req]
  (let [username (:session req)]
    (if (seq username)
      (render-file "views/dashboard.html" {:user (m/get-user db username)})
      (header {} "HX-Location" "/login"))))

(defn logout []
  (header {:session nil} "HX-Location" "/login"))
