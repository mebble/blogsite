(ns blogsite-clj.model.user
  (:require
   [blogsite-clj.mappers.user :refer [db->domain]]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]))

(defn get-user [db username]
  (->> (sql/query db ["select * from users where username = ?" username])
       (first)
       (db->domain)))

(defn insert-user [db user]
  (->> (jdbc/execute-one!
        db
        ["insert into users (username, password) values (?, ?) returning *"
         (:username user)
         (:password user)])
       (db->domain)))
