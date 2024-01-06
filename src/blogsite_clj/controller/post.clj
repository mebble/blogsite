(ns blogsite-clj.controller.post
  (:require
   [blogsite-clj.model.post :as m]
   [cats.monad.either :as e]
   [ring.util.response :refer [header not-found redirect status]]
   [selmer.parser :refer [render-file]]
   [sluj.core :refer [sluj]]))

(defn get-post-creation [req]
  (let [username (get-in req [:session :username])]
    (render-file "views/new.html" {:username username})))

(defn get-posts [db req]
  (render-file "views/posts.html" {:posts (m/get-posts db) :session (:session req)}))

(defn- redirect-url [id slug]
  (str "/posts/" id "/" slug))

(defn get-post [db id slug]
  (if-let [post (m/get-post db id)]
    (if (= slug (:slug post))
      (let [comments (m/get-comments db id)]
        (render-file "views/post.html" {:post post :comments comments}))
      (let [url (redirect-url (:id post) (:slug post))]
        (redirect url)))
    (not-found "no such blog post")))

(defn post-new-post [db req]
  (let [user_id  (get-in req [:session :user_id])
        params   (:params req)
        slug     (sluj (:title params ""))
        new-post (-> (select-keys params [:title :description :contents])
                     (assoc :slug slug))]
    (e/branch (m/save-post db new-post user_id)
              (fn [_] (status 500))
              (fn [post-id] (let [url (redirect-url post-id slug)]
                              ;; [?] An ordinary redirect after POST doesn't seem to work, but not sure. Must revisit
                              (header {} "HX-Location" url))))))

(defn post-new-comment [db req]
  (let [user_id  (get-in req [:session :user_id])
        username (get-in req [:session :username])
        new-commentt (-> (select-keys (:params req) [:contents :post_id])
                         (update :post_id parse-long)
                         (assoc :user_id user_id))]
    (e/branch (m/save-comment db new-commentt)
              (fn [_] (status 500))
              (fn [commentt] (render-file "views/comment.html" {:comment (assoc commentt :username username)})))))
