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
   [ring.middleware.session :refer [wrap-session]]
   [jdbc-ring-session.core :refer [jdbc-store]]
   [selmer.parser :refer [cache-off!]]))

(def db (get-datasource {:dbtype "sqlite" :dbname "blog.db"}))

(defn env [key]
  (some-> (System/getenv key)
          (read-string)))

(def handler
  (routes
   (GET "/login" [] (c/login-page))
   (POST "/login" req (c/login req))
   (GET "/blogs" [] (c/get-blogs db))
   (GET "/new", req (c/get-blog-creation req))
   (POST "/blogs" req (c/post-new-blog db req))
   (GET "/blogs/:id/:slug" [id slug] (c/get-blog db id slug))
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
