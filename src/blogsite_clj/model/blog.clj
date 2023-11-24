(ns blogsite-clj.model.blog
  (:require
   [clojure.set :refer [rename-keys]]
   [next.jdbc.sql :as sql]
   [cats.monad.either :as e]
   [cats.monad.maybe :as m]
   [next.jdbc :as jdbc]))

(defmacro try-either [expr]
  (list
   'try
   (list 'e/right expr)
   '(catch Exception e (e/left e))))

(defmacro try-maybe [expr]
  (list 'let ['v expr]
        '(if (nil? v)
           (m/nothing)
           (m/just v))))

(defn- map-keys [m]
  (rename-keys m {:blogs/rowid :id
                  :blogs/slug :slug
                  :blogs/title :title
                  :blogs/description :description
                  :blogs/contents :contents}))

(defn get-blogs [db]
  (->> (sql/query db ["select rowid, * from blogs"])
       (map map-keys)))

(defn get-blog [db slug]
  (->> (sql/query db ["select rowid, * from blogs where slug = ?" slug])
       (first)
       (map-keys)))

(defn save-blog [db blog]
  (try-either
   (->> (jdbc/execute-one!
         db
         ["insert into blogs (slug, title, description, contents) values (?, ?, ?, ?) returning rowid"
          (:slug blog)
          (:title blog)
          (:description blog)
          (:contents blog)])
        (:blogs/rowid))))
