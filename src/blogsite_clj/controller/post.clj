(ns blogsite-clj.controller.post
  (:require
   [blogsite-clj.mappers.post :refer [comment:http->domain http->domain]]
   [blogsite-clj.model.post :as m]
   [cats.monad.either :as e]
   [ring.util.response :refer [header not-found redirect status]]
   [selmer.parser :refer [render-file]]))

(defn get-post-creation [req]
  (let [username (get-in req [:session :username])]
    (render-file "views/new.html" {:username username})))

(defn get-posts [db req]
  (render-file "views/posts.html" {:posts (m/get-posts db) :session (:session req)}))

(defn- redirect-url [id slug]
  (str "/posts/" id "/" slug))

(defn get-post [db req]
  (let [id (get-in req [:params :id])
        slug (get-in req [:params :slug])]
    (if-let [post (m/get-post db id)]
      (if (= slug (:slug post))
        (let [comments (m/get-comments db id)]
          (render-file "views/post.html" {:post post :comments comments :session (:session req)}))
        (let [url (redirect-url (:id post) (:slug post))]
          (redirect url)))
      (not-found "no such blog post"))))

(defn post-new-post [db req]
  (let [new-post (http->domain req)]
    (e/branch (m/save-post db new-post)
              (fn [_] (status 500))
              (fn [{:keys [id slug]}]
                (let [url (redirect-url id slug)]
                  ;; [?] An ordinary redirect after POST doesn't seem to work, but not sure. Must revisit
                  (header {} "HX-Location" url))))))

(defn post-new-comment [db req]
  (let [username (get-in req [:session :username])
        new-commentt (comment:http->domain req)]
    (e/branch (m/save-comment db new-commentt)
              (fn [_] (status 500))
              (fn [commentt] (render-file "views/comment.html" {:comment (assoc commentt :username username)})))))
