(ns blogsite-clj.core
  (:gen-class)
  (:require
   [blogsite-clj.model.blog :refer [get-blogs]]
   [compojure.core :refer [GET POST routes]]
   [compojure.route :refer [not-found]]
   [next.jdbc :as jdbc]
   [ring.adapter.jetty :refer [run-jetty]]
   [selmer.parser :refer [render-file]]))

(def db (jdbc/get-datasource {:dbtype "sqlite" :dbname "blog.db"}))

(def handler
  (routes
   (GET "/" [] (do (prn (get-blogs db)) (str "hi")))
   (GET "/user/:id" [id] (render-file "views/home.html" {:name id}))
   (POST "/clicked" [] (str "You clicked me!"))
   (not-found "Not found")))

(defn -main
  "I don't do a whole lot ... yet."
  [& _args]
  (run-jetty handler {:port 4000 :join? false}))
