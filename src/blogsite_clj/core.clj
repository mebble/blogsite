(ns blogsite-clj.core
  (:gen-class)
  (:require
   [blogsite-clj.controller.blog :as c]
   [compojure.core :refer [GET POST routes]]
   [compojure.route :as r]
   [next.jdbc :refer [get-datasource]]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.refresh :refer [wrap-refresh]]
   [ring.middleware.reload :refer [wrap-reload]]
   [selmer.parser :refer [cache-off!]]))

(def db (get-datasource {:dbtype "sqlite" :dbname "blog.db"}))

(defn env [key]
  (some-> (System/getenv key)
          (read-string)))

(def handler
  (routes
   (GET "/blogs" [] (c/get-blogs db))
   (GET "/new", [] (c/get-blog-creation))
   (POST "/blogs" req (c/post-new-blog db req))
   (GET "/blogs/:id/:slug" [id slug] (c/get-blog db id slug))
   (r/not-found "Not found")))

(def app (-> handler
             ;; ordering of the middleware matters
             (wrap-keyword-params)
             (wrap-params)))

(when (env "DEVELOPMENT")
  (println "Development mode")
  (cache-off!)
  (def app (-> app
               (wrap-refresh)
               (wrap-reload))))

(defn -main
  [& _args]
  (run-jetty app {:port 4000 :join? false}))
