(ns blogsite-clj.core
  (:gen-class)
  (:require
   [blogsite-clj.model.blog :refer [get-blog get-blogs]]
   [compojure.core :refer [GET routes]]
   [compojure.route :refer [not-found]]
   [next.jdbc :as jdbc]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.refresh :refer [wrap-refresh]]
   [selmer.parser :refer [render-file cache-off!]]))

(def db (jdbc/get-datasource {:dbtype "sqlite" :dbname "blog.db"}))

(defn env [key]
  (read-string (System/getenv key)))

(def handler
  (routes
   (GET "/" [] (render-file "views/home.html" {:blogs (get-blogs db)}))
   (GET "/blogs/:slug" [slug] (render-file "views/blog.html" {:blog (get-blog db slug)}))
   (not-found "Not found")))

(when (env "DEVELOPMENT")
  (cache-off!)
  (def handler (wrap-refresh handler)))

(defn -main
  [& _args]
  (run-jetty handler {:port 4000 :join? false}))
