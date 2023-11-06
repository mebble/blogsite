(ns blogsite-clj.core
  (:gen-class)
  (:require [ring.adapter.jetty :refer [run-jetty]]))

(defn handler [_req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World"})

(defn -main
  "I don't do a whole lot ... yet."
  [& _args]
  (run-jetty handler {:port 4000 :join? false}))
