(ns blogsite-clj.controller.blog
  (:require
   [blogsite-clj.model.blog :as m]
   [cats.monad.either :as e]
   [ring.util.response :refer [header not-found redirect status]]
   [selmer.parser :refer [render-file]]
   [sluj.core :refer [sluj]]))

(defn get-blog-creation [req]
  (let [username (get-in req [:session :username])]
    (render-file "views/new.html" {:username username})))

(defn get-blogs [db req]
  (render-file "views/blogs.html" {:blogs (m/get-blogs db) :session (:session req)}))

(defn- redirect-url [id slug]
  (str "/blogs/" id "/" slug))

(defn get-blog [db id slug]
  (if-let [blog (m/get-blog db id)]
    (if (= slug (:slug blog))
      (let [comments (m/get-comments db id)]
        (render-file "views/blog.html" {:blog blog :comments comments}))
      (let [url (redirect-url (:id blog) (:slug blog))]
        (redirect url)))
    (not-found "no such blog post")))

(defn post-new-blog [db req]
  (let [user_id  (get-in req [:session :user_id])
        params   (:params req)
        slug     (sluj (:title params ""))
        new-blog (-> (select-keys params [:title :description :contents])
                     (assoc :slug slug))]
    (e/branch (m/save-blog db new-blog user_id)
              (fn [_] (status 500))
              (fn [blog-id] (let [url (redirect-url blog-id slug)]
                              ;; [?] An ordinary redirect after POST doesn't seem to work, but not sure. Must revisit
                              (header {} "HX-Location" url))))))

(defn post-new-comment [db req]
  (let [user_id  (get-in req [:session :user_id])
        username (get-in req [:session :username])
        new-commentt (-> (select-keys (:params req) [:contents :blog_id])
                         (update :blog_id parse-long)
                         (assoc :user_id user_id))]
    (e/branch (m/save-comment db new-commentt)
              (fn [_] (status 500))
              (fn [commentt] (render-file "views/comment.html" {:comment (assoc commentt :username username)})))))
