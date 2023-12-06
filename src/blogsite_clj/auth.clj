(ns blogsite-clj.auth
  (:require
   [ring.util.response :refer [header redirect]]))

(defn auth-route [req handler]
  (let [session (:session req)]
    (cond
      (seq session)                  (handler session)
      (= :get (:request-method req)) (redirect "/login")
      :else                          (header {} "HX-Location" "/login"))))
