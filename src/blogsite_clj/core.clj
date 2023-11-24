(ns blogsite-clj.core
  (:gen-class)
  (:require
   [blogsite-clj.model.blog :refer [get-blog get-blogs save-blog]]
   [cats.monad.either :as e]
   [compojure.core :refer [GET POST routes]]
   [compojure.route :as r]
   [next.jdbc :as jdbc]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.refresh :refer [wrap-refresh]]
   [ring.util.response :refer [header status not-found]]
   [selmer.parser :refer [cache-off! render-file]]
   [sluj.core :refer [sluj]]))

(def db (jdbc/get-datasource {:dbtype "sqlite" :dbname "blog.db"}))

(defn env [key]
  (some-> (System/getenv key)
          (read-string)))

(defn keys-to-keywords [m]
  (into {} (map (fn [[k v]] (vector (keyword k) v)) m)))

(def handler
  (wrap-params
   (routes
    (GET "/blogs" [] (render-file "views/home.html" {:blogs (get-blogs db)}))
    (GET "/new", [] (render-file "views/new.html" {}))
    (POST "/blogs" req (let [params (:form-params req)
                             slug (sluj (get params "title"))
                             new-blog (-> (select-keys params ["title" "description" "contents"])
                                          (keys-to-keywords)
                                          (assoc :slug slug))
                             url (str "/blogs/" slug)]
                         (e/branch (save-blog db new-blog)
                                   (fn [_] (status 500))
                                   (fn [_] (header {} "HX-Location" url)))))
    (GET "/blogs/:slug" [slug] (if-let [blog (get-blog db slug)]
                                 (render-file "views/blog.html" {:blog blog})
                                 (not-found "no such blog post")))
    (r/not-found "Not found"))))

(when (env "DEVELOPMENT")
  (println "Development mode")
  (cache-off!)
  (def handler (wrap-refresh handler)))

(defn -main
  [& _args]
  (run-jetty handler {:port 4000 :join? false}))
