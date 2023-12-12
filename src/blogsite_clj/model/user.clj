(ns blogsite-clj.model.user
  (:require
   [clojure.set :refer [rename-keys]]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]))

(defn- map-keys [m]
  (rename-keys m {:users/id       :id
                  :users/username :username
                  :users/password :password}))

(defn get-user [db username]
  (->> (sql/query db ["select * from users where username = ?" username])
       (first)
       (map-keys)))

(defn insert-user [db user]
  (->> (jdbc/execute-one!
        db
        ["insert into users (username, password) values (?, ?) returning *"
         (:username user)
         (:password user)])
       (map-keys)))
