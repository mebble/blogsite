(ns blogsite-clj.core
  (:gen-class)
  (:require
   [blogsite-clj.model.blog :refer [get-blog get-blogs save-blog]]
   [compojure.core :refer [GET POST routes]]
   [compojure.route :refer [not-found]]
   [next.jdbc :as jdbc]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.refresh :refer [wrap-refresh]]
   [ring.util.response :refer [header]]
   [selmer.parser :refer [cache-off! render-file]]
   [sluj.core :refer [sluj]]))

(def db (jdbc/get-datasource {:dbtype "sqlite" :dbname "blog.db"}))

(defn env [key]
  (read-string (System/getenv key)))

(defn keys-to-keywords [m]
  (into {} (map (fn [[k v]] (vector (keyword k) v)) m)))

(def handler
  (routes
   (GET "/blogs" [] (render-file "views/home.html" {:blogs (get-blogs db)}))
   (GET "/new", [] (render-file "views/new.html" {}))
   (POST "/blogs" req (let [params (:form-params req)
                            slug (sluj (get params "title"))
                            new-blog (-> (select-keys params ["title" "description" "contents"])
                                         (keys-to-keywords)
                                         (assoc :slug slug))
                            url (str "/blogs/" slug)]
                        (save-blog db new-blog)
                        (header {} "HX-Location" url)))
   (GET "/blogs/:slug" [slug] (render-file "views/blog.html" {:blog (get-blog db slug)}))
   (not-found "Not found")))

(when (env "DEVELOPMENT")
  (cache-off!)
  (def handler (wrap-refresh handler)))

(defn -main
  [& _args]
  (run-jetty handler {:port 4000 :join? false}))
