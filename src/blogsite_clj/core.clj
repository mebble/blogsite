(ns blogsite-clj.core
  (:gen-class)
  (:require
   [blogsite-clj.controller.blog :as c]
   [compojure.core :refer [GET POST routes]]
   [compojure.route :as r]
   [next.jdbc :refer [get-datasource]]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.refresh :refer [wrap-refresh]]
   [selmer.parser :refer [cache-off!]]))

(def db (get-datasource {:dbtype "sqlite" :dbname "blog.db"}))

(defn env [key]
  (some-> (System/getenv key)
          (read-string)))

(def handler
  (wrap-params
   (routes
    (GET "/blogs" [] (c/get-blogs db))
    (GET "/new", [] (c/get-blog-creation))
    (POST "/blogs" req (c/post-new-blog db req))
    (GET "/blogs/:id/:slug" [_id slug] (c/get-blog db slug))
    (r/not-found "Not found"))))

(when (env "DEVELOPMENT")
  (println "Development mode")
  (cache-off!)
  (def handler (wrap-refresh handler)))

(defn -main
  [& _args]
  (run-jetty handler {:port 4000 :join? false}))
