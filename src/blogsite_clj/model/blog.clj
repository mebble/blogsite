(ns blogsite-clj.model.blog
  (:require [next.jdbc.sql :as sql]))

(defn get-blogs [db]
  (sql/query db ["select * from blogs"]))
