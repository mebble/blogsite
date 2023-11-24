(ns blogsite-clj.model.blog
  (:require
   [clojure.set :refer [rename-keys]]
   [next.jdbc.sql :as sql]
   [cats.monad.either :as e]
   [cats.monad.maybe :as m]))

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
  (rename-keys m {:blogs/slug :slug
                  :blogs/title :title
                  :blogs/description :description
                  :blogs/contents :contents}))

(defn get-blogs [db]
  (->> (sql/query db ["select * from blogs"])
       (map map-keys)))

(defn get-blog [db slug]
  (->> (sql/query db ["select * from blogs where slug = ?" slug])
       (first)
       (map-keys)))

(defn save-blog [db blog]
  (try-either
   (->> (sql/insert! db :blogs blog))))
