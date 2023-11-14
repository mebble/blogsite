(ns blogsite-clj.core
  (:gen-class)
  (:require
   [blogsite-clj.model.blog :refer [get-blog get-blogs]]
   [compojure.core :refer [GET routes]]
   [compojure.route :refer [not-found]]
   [next.jdbc :as jdbc]
   [ring.adapter.jetty :refer [run-jetty]]
   [selmer.parser :refer [render-file]]))

(def db (jdbc/get-datasource {:dbtype "sqlite" :dbname "blog.db"}))

(def handler
  (routes
   (GET "/" [] (render-file "views/home.html" {:blogs (get-blogs db)}))
   (GET "/blogs/:slug" [slug] (render-file "views/blog.html" {:blog (get-blog db slug)}))
   (not-found "Not found")))

(defn -main
  "I don't do a whole lot ... yet."
  [& _args]
  (run-jetty handler {:port 4000 :join? false}))
