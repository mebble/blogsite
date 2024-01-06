(ns blogsite-clj.core
  (:gen-class)
  (:require
   [blogsite-clj.auth :as a]
   [blogsite-clj.controller.post :as c-post]
   [blogsite-clj.controller.user :as c-user]
   [compojure.core :refer [GET POST routes]]
   [compojure.route :as r]
   [jdbc-ring-session.core :refer [jdbc-store]]
   [next.jdbc :refer [get-datasource execute-one!]]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.refresh :refer [wrap-refresh]]
   [ring.middleware.session :refer [wrap-session]]
   [selmer.parser :refer [cache-off!]]))

(def db (get-datasource {:dbtype "sqlite" :dbname "blog.db"}))
(execute-one! db ["PRAGMA foreign_keys = ON"]) ;; must do this per connection to the db, hence can't put this in migration file

(defn env [key]
  (some-> (System/getenv key)
          (read-string)))

(def handler
  (routes
   (GET "/" req (c-post/get-posts db req))
   (GET "/posts" req (c-post/get-posts db req))
   (GET "/login" [] (c-user/login-page))
   (POST "/login" req (c-user/login db req))
   (POST "/logout" [] (c-user/logout))
   (GET "/dashboard" req (a/auth-route req (fn [req] (c-user/dashboard db req))))
   (GET "/new", req (a/auth-route req (fn [req] (c-post/get-post-creation req))))
   (POST "/posts" req (a/auth-route req (fn [req] (c-post/post-new-post db req))))
   (POST "/comments" req (a/auth-route req (fn [req] (c-post/post-new-comment db req))))
   (GET "/posts/:id/:slug" [id slug] (c-post/get-post db id slug))
   (GET "/:username" [username] (c-user/user-page db username))
   (r/not-found "Not found")))

(def app (-> handler
             (wrap-session {:store (jdbc-store db)})
             ;; wrap-params before wrap-keyword-params
             (wrap-keyword-params)
             (wrap-params)))

(when (env "DEVELOPMENT")
  (println "Development mode")
  (cache-off!)
  (def app (-> app
               (wrap-refresh))))

(defn -main
  [& _args]
  (run-jetty app {:port 4000 :join? false}))
