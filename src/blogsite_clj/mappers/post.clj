(ns blogsite-clj.mappers.post
  (:require
   [clojure.set :refer [rename-keys]]
   [sluj.core :refer [sluj]]))

(defn- to-base36 [num]
  (Integer/toString num 36))

(defmacro try-with-default [expr default-value]
  `(try ~expr (catch Exception e# ~default-value)))

(defmacro non-nil-default [expr default-value]
  `(let [res# ~expr]
     (if (nil? res#)
       ~default-value
       res#)))

(defmacro safe-parse [expr default-value]
  `(-> ~expr
       (try-with-default ~default-value)
       (non-nil-default ~default-value)))

(defn http->domain [http-req]
  (let [params (:params http-req)]
    (-> (select-keys params [:title :description :contents])
        (assoc :id          (:post-id params))
        (assoc :published   (safe-parse (parse-boolean (:published params)) true))
        (assoc :user_id     (get-in http-req [:session :user_id]))
        (assoc :username    (get-in http-req [:session :username]))
        (assoc :slug        (sluj (:title params ""))))))

(defn db->domain [db-resp]
  (-> db-resp
      (rename-keys {:posts/id          :id
                    :posts/slug        :slug
                    :posts/title       :title
                    :posts/description :description
                    :posts/contents    :contents
                    :posts/user_id     :user_id
                    :posts/published   :published
                    :users/username    :username})
      (update :id to-base36)))

(defn comment:http->domain [req]
  (let [user_id (get-in req [:session :user_id])]
    (-> (select-keys (:params req) [:contents :post_id])
        (update :post_id parse-long)
        (assoc :user_id user_id))))

(defn comment:db->domain [c]
  (rename-keys c {:comments/id :id,
                  :comments/contents :contents,
                  :comments/user_id :user_id,
                  :comments/post_id :post_id
                  :users/username :username}))
