(ns blogsite-clj.core
  (:gen-class)
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer [GET routes]]
            [compojure.route :refer [not-found]]))

(def handler
  (routes
   (GET "/user/:id" [id] (str "Hello " id))
   (not-found "Not found")))

(defn -main
  "I don't do a whole lot ... yet."
  [& _args]
  (run-jetty handler {:port 4000 :join? false}))
