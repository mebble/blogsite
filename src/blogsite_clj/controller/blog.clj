(ns blogsite-clj.controller.blog
  (:require
   [blogsite-clj.model.blog :as model]
   [cats.monad.either :as e]
   [ring.util.response :refer [header not-found redirect status]]
   [selmer.parser :refer [render-file]]
   [sluj.core :refer [sluj]]))

(defn get-blog-creation []
  (render-file "views/new.html" {}))

(defn get-blogs [db]
  (render-file "views/home.html" {:blogs (model/get-blogs db)}))

(defn- redirect-url [id slug]
  (str "/blogs/" id "/" slug))

(defn get-blog [db id slug]
  (if-let [blog (model/get-blog db id)]
    (if (= slug (:slug blog))
      (render-file "views/blog.html" {:blog blog})
      (let [url (redirect-url (:id blog) (:slug blog))]
        (redirect url)))
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
              (fn [blog-id] (let [url (redirect-url blog-id slug)]
                              ;; [?] An ordinary redirect after POST doesn't seem to work, but not sure. Must revisit
                              (header {} "HX-Location" url))))))
