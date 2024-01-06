(ns blogsite-clj.model.post
  (:require
   [clojure.set :refer [rename-keys]]
   [next.jdbc.sql :as sql]
   [cats.monad.either :as e]
   [next.jdbc :as jdbc]))

(defmacro try-either [expr]
  (list
   'try
   (list 'e/right expr)
   '(catch Exception e (e/left e))))

(defn- to-base36 [num]
  (Integer/toString num 36))

(defn- from-base36 [s]
  (Integer/parseInt s 36))

(defn- map-post-to-domain [m]
  (-> m
      (rename-keys {:posts/id :id
                    :posts/slug :slug
                    :posts/title :title
                    :posts/description :description
                    :posts/contents :contents
                    :posts/user_id  :user_id
                    :users/username :username})
      (update :id to-base36)))

(defn get-posts [db]
  (->> (sql/query db ["select posts.*, users.username from posts left join users on posts.user_id = users.id"])
       (map map-post-to-domain)))

(defn get-post [db id-str]
  (let [id (from-base36 id-str)]
    (some->> (sql/query db ["select posts.*, users.username from posts left join users on posts.user_id = users.id where posts.id = ?" id])
             (first)
             (map-post-to-domain))))

(defn get-posts-by-user [db user-id]
  (->> (sql/query db ["select posts.*, users.username from posts left join users on posts.user_id = users.id where posts.user_id = ?" user-id])
       (map map-post-to-domain)))

(defn save-post [db post user_id]
  (try-either
   (->> (jdbc/execute-one!
         db
         ["insert into posts (slug, title, description, contents, user_id) values (?, ?, ?, ?, ?) returning id"
          (:slug post)
          (:title post)
          (:description post)
          (:contents post)
          user_id])
        (:posts/id)
        (to-base36))))

(defn- map-comment-to-domain [c]
  (rename-keys c {:comments/id :id,
                  :comments/contents :contents,
                  :comments/user_id :user_id,
                  :comments/post_id :post_id
                  :users/username :username}))

(defn save-comment [db commentt]
  (try-either
   (->> (jdbc/execute-one!
         db
         ["insert into comments (contents, user_id, post_id) values (?, ?, ?) returning *"
          (:contents commentt)
          (:user_id commentt)
          (:post_id commentt)])
        (map-comment-to-domain))))

(defn get-comments [db post_id]
  (->> (sql/query db ["select comments.*, users.username from comments left join users on comments.user_id = users.id where comments.post_id = ?" post_id])
       (map map-comment-to-domain)))
