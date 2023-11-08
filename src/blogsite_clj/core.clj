(ns blogsite-clj.core
  (:gen-class)
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer [GET POST routes]]
            [compojure.route :refer [not-found]]
            [selmer.parser :refer [render-file]]))

(def handler
  (routes
   (GET "/user/:id" [id] (render-file "home.html" {:name id}))
   (POST "/clicked" [] (str "You clicked me!"))
   (not-found "Not found")))

(defn -main
  "I don't do a whole lot ... yet."
  [& _args]
  (run-jetty handler {:port 4000 :join? false}))
