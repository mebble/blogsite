(ns blogsite-clj.mappers.user
  (:require
   [buddy.hashers :as hashers]
   [clojure.set :refer [rename-keys]]))

(defn http->domain [req]
  (-> (select-keys (:params req) [:username :password])
      (update :password (fn [p] (hashers/derive p {:alg :bcrypt+blake2b-512})))))

(defn db->domain [m]
  (rename-keys m {:users/id       :id
                  :users/username :username
                  :users/password :password}))
