(ns blogsite-clj.model.blog
  (:require
   [clojure.set :refer [rename-keys]]
   [next.jdbc.sql :as sql]))

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
  (->> (sql/insert! db :blogs blog)))
