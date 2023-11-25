(ns blogsite-clj.controller.blog
  (:require
   [blogsite-clj.model.blog :as model]
   [cats.monad.either :as e]
   [ring.util.response :refer [header not-found status]]
   [selmer.parser :refer [render-file]]
   [sluj.core :refer [sluj]]))

(defn get-blog-creation []
  (render-file "views/new.html" {}))

(defn get-blogs [db]
  (render-file "views/home.html" {:blogs (model/get-blogs db)}))

(defn get-blog [db slug]
  (if-let [blog (model/get-blog db slug)]
    (render-file "views/blog.html" {:blog blog})
    (not-found "no such blog post")))

(defn- keys-to-keywords [m]
  (into {} (map (fn [[k v]] (vector (keyword k) v)) m)))

(defn post-new-blog [db req]
  (let [params   (:form-params req)
        slug     (sluj (get params "title"))
        new-blog (-> (select-keys params ["title" "description" "contents"])
                     (keys-to-keywords)
                     (assoc :slug slug))]
    (e/branch (model/save-blog db new-blog)
              (fn [_] (status 500))
              (fn [blog-id] (let [url (str "/blogs/" blog-id "/" slug)]
                              (header {} "HX-Location" url))))))
