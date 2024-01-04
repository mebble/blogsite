(ns blogsite-clj.auth
  (:require
   [ring.util.response :refer [header redirect]]))

(defn create-session [user]
  {:user_id (:id user)
   :username (:username user)})

(defn auth-route [req handler]
  (let [session (:session req)]
    (cond
      (seq session)                  (handler req)
      (= :get (:request-method req)) (redirect "/login")
      :else                          (header {} "HX-Location" "/login"))))
