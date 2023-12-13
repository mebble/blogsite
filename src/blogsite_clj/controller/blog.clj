(ns blogsite-clj.controller.blog
  (:require
   [blogsite-clj.model.blog :as m]
   [cats.monad.either :as e]
   [ring.util.response :refer [header not-found redirect status]]
   [selmer.parser :refer [render-file]]
   [sluj.core :refer [sluj]]))

(defn get-blog-creation [username]
  (render-file "views/new.html" {:username username}))

(defn get-blogs [db]
  (render-file "views/blogs.html" {:blogs (m/get-blogs db)}))

(defn- redirect-url [id slug]
  (str "/blogs/" id "/" slug))

(defn get-blog [db id slug]
  (if-let [blog (m/get-blog db id)]
    (if (= slug (:slug blog))
      (render-file "views/blog.html" {:blog blog})
      (let [url (redirect-url (:id blog) (:slug blog))]
        (redirect url)))
    (not-found "no such blog post")))

(defn post-new-blog [db req user_id]
  (let [params   (:params req)
        slug     (sluj (:title params ""))
        new-blog (-> (select-keys params [:title :description :contents])
                     (assoc :slug slug))]
    (e/branch (m/save-blog db new-blog user_id)
              (fn [_] (status 500))
              (fn [blog-id] (let [url (redirect-url blog-id slug)]
                              ;; [?] An ordinary redirect after POST doesn't seem to work, but not sure. Must revisit
                              (header {} "HX-Location" url))))))

(defn post-new-comment [db req user_id]
  (let [new-commentt (-> (select-keys (:params req) [:contents :blog_id])
                         (update :blog_id parse-long)
                         (assoc :user_id user_id))]
    (e/branch (m/save-comment db new-commentt)
              (fn [_] (status 500))
              (fn [commentt] (render-file "views/comment.html" {:comment commentt})))))
