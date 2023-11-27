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

(defn- map-keys [m]
  (rename-keys m {:blogs/rowid :id
                  :blogs/slug :slug
                  :blogs/title :title
                  :blogs/description :description
                  :blogs/contents :contents}))

(defn- map-to-domain [m]
  (update m :id to-base36))

(defn get-blogs [db]
  (->> (sql/query db ["select rowid, * from blogs"])
       (map map-keys)
       (map map-to-domain)))

(defn get-blog [db id]
  (let [rowid (from-base36 id)]
    (some->> (sql/query db ["select rowid, * from blogs where rowid = ?" rowid])
             (first)
             (map-keys)
             (map-to-domain))))

(defn save-blog [db blog]
  (try-either
   (->> (jdbc/execute-one!
         db
         ["insert into blogs (slug, title, description, contents) values (?, ?, ?, ?) returning rowid"
          (:slug blog)
          (:title blog)
          (:description blog)
          (:contents blog)])
        (:blogs/rowid)
        (to-base36))))
