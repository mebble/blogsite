(ns blogsite-clj.model.blog
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

(defn- map-blog-to-domain [m]
  (-> m
      (rename-keys {:blogs/id :id
                    :blogs/slug :slug
                    :blogs/title :title
                    :blogs/description :description
                    :blogs/contents :contents
                    :blogs/user_id  :user_id
                    :users/username :username})
      (update :id to-base36)))

(defn get-blogs [db]
  (->> (sql/query db ["select blogs.*, users.username from blogs left join users on blogs.user_id = users.id"])
       (map map-blog-to-domain)))

(defn get-blog [db id-str]
  (let [id (from-base36 id-str)]
    (some->> (sql/query db ["select blogs.*, users.username from blogs left join users on blogs.user_id = users.id where blogs.id = ?" id])
             (first)
             (map-blog-to-domain))))

(defn get-blogs-by-user [db user-id]
  (->> (sql/query db ["select blogs.*, users.username from blogs left join users on blogs.user_id = users.id where blogs.user_id = ?" user-id])
       (map map-blog-to-domain)))

(defn save-blog [db blog user_id]
  (try-either
   (->> (jdbc/execute-one!
         db
         ["insert into blogs (slug, title, description, contents, user_id) values (?, ?, ?, ?, ?) returning id"
          (:slug blog)
          (:title blog)
          (:description blog)
          (:contents blog)
          user_id])
        (:blogs/id)
        (to-base36))))

(defn- map-comment-to-domain [c]
  (rename-keys c {:comments/id :id,
                  :comments/contents :contents,
                  :comments/user_id :user_id,
                  :comments/blog_id :blog_id}))

(defn save-comment [db commentt]
  (try-either
   (->> (jdbc/execute-one!
         db
         ["insert into comments (contents, user_id, blog_id) values (?, ?, ?) returning *"
          (:contents commentt)
          (:user_id commentt)
          (:blog_id commentt)])
        (map-comment-to-domain))))

(defn get-comments [db blog_id]
  (->> (sql/query db ["select * from comments where blog_id = ?" blog_id])
       (map map-comment-to-domain)))
