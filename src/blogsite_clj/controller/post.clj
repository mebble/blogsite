(ns blogsite-clj.controller.post
  (:require
   [blogsite-clj.mappers.post :refer [comment:http->domain http->domain]]
   [blogsite-clj.model.post :as m]
   [cats.monad.either :as e]
   [ring.util.response :refer [header not-found redirect status]]
   [selmer.parser :refer [render-file]]))

(defn get-post-creation [req]
  (render-file "views/new.html" {:session (:session req)}))

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

(defn get-edit-page [db req]
  (let [id (get-in req [:params :id])
        slug (get-in req [:params :slug])
        user-id (get-in req [:session :user_id])]
    (if-let [post (m/get-post-by-user db user-id id)]
      (if (= slug (:slug post))
        (render-file "views/edit.html" {:post post :session (:session req)})
        (let [url (redirect-url (:id post) (:slug post))]
          (redirect url)))
      (not-found "no such blog post"))))

(defn update-post [db req]
  (let [updated-post (http->domain req)]
    (e/branch (m/update-post db updated-post)
              (fn [_] (status 500))
              (fn [_]
                (let [url (redirect-url (:id updated-post) (:slug updated-post))]
                  (header {} "HX-Location" url))))))

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
