(ns blogsite-clj.controller.blog
  (:require
   [blogsite-clj.model.blog :as m]
   [cats.monad.either :as e]
   [ring.util.response :refer [header not-found redirect status]]
   [selmer.parser :refer [render-file]]
   [sluj.core :refer [sluj]]))

(defn get-blog-creation [req]
  (let [user (:session req)]
    (if (seq user)
      (render-file "views/new.html" {:username (:name user)})
      (redirect "/login"))))

(defn get-blogs [db]
  (render-file "views/blogs.html" {:blogs (m/get-blogs db)}))

(defn- redirect-url [id slug]
  (str "/blogs/" id "/" slug))

(defn get-blog [db id slug]
  (if-let [blog (m/get-blog db id)]
    (if (= slug (:slug blog))
      (render-file "views/blog.html" {:blog blog})
      (let [url (redirect-url (:id blog) (:slug blog))]
        (redirect url)))
    (not-found "no such blog post")))

(defn post-new-blog [db req]
  (let [user (:session req)]
    (if (seq user)
      (let [params   (:params req)
            slug     (sluj (:title params ""))
            new-blog (-> (select-keys params [:title :description :contents])
                         (assoc :slug slug))]
        (e/branch (m/save-blog db new-blog)
                  (fn [_] (status 500))
                  (fn [blog-id] (let [url (redirect-url blog-id slug)]
                              ;; [?] An ordinary redirect after POST doesn't seem to work, but not sure. Must revisit
                                  (header {} "HX-Location" url)))))
      (header {} "HX-Location" "/login"))))

(def userinfo {:name "john"
               :email "john@example.com"})

(defn login-page []
  (render-file "views/login.html" {}))

(defn login [req]
  (let [password (-> req :params :password)]
    (if (= password "abc")
      {:body "Hi there"
       :session userinfo}
      {:body "Wrong password"})))
