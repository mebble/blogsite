(ns blogsite-clj.model.post
  (:require
   [blogsite-clj.mappers.post :refer [comment:db->domain db->domain]]
   [cats.monad.either :as e]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]))

(defn- from-base36 [s]
  (Integer/parseInt s 36))

(defn get-posts [db]
  (->> (sql/query db ["select posts.*, users.username from posts left join users on posts.user_id = users.id"])
       (map db->domain)))

(defn get-post [db id-str]
  (let [id (from-base36 id-str)]
    (some->> (sql/query db ["select posts.*, users.username from posts left join users on posts.user_id = users.id where posts.id = ?" id])
             (first)
             (db->domain))))

(defn get-post-by-user [db user-id post-id]
  (let [post-db-id (from-base36 post-id)]
    (some->> (sql/query db ["select * from posts where id = ? and user_id = ?" post-db-id user-id])
             (first)
             (db->domain))))

(defn get-posts-by-user [db user-id]
  (->> (sql/query db ["select posts.*, users.username from posts left join users on posts.user_id = users.id where posts.user_id = ?" user-id])
       (map db->domain)))

(defn save-post [db post]
  (e/try-either
   (->> (jdbc/execute-one!
         db
         ["insert into posts (slug, title, description, contents, user_id) values (?, ?, ?, ?, ?) returning *"
          (:slug post)
          (:title post)
          (:description post)
          (:contents post)
          (:user_id post)])
        (db->domain))))

(defn save-comment [db commentt]
  (e/try-either
   (->> (jdbc/execute-one!
         db
         ["insert into comments (contents, user_id, post_id) values (?, ?, ?) returning *"
          (:contents commentt)
          (:user_id commentt)
          (:post_id commentt)])
        (comment:db->domain))))

(defn get-comments [db post-id-str]
  (let [id (from-base36 post-id-str)]
    (->> (sql/query db ["select comments.*, users.username from comments left join users on comments.user_id = users.id where comments.post_id = ?" id])
         (map comment:db->domain))))

(defn- did-update? [res]
  (-> res
      (:next.jdbc/update-count)
      (zero?)
      (not)))

(defn update-post [db post]
  (let [id (from-base36 (:id post))
        user-id (:user_id post)]
    (e/branch-right
     (e/try-either
      (->> (jdbc/execute-one!
            db
            ["update posts set (slug, title, description, contents) = (?, ?, ?, ?) where id = ? and user_id = ?"
             (:slug post)
             (:title post)
             (:description post)
             (:contents post)
             id
             user-id])))
     (fn [res] (if (did-update? res)
                 (e/right)
                 (e/left))))))
