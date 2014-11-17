(ns routes.core-test
  #+cljs (:require-macros [cemerick.cljs.test :refer [deftest is are]]
                          [routes.core :refer [defroutes]])
  (:require [routes.core :as routes]
            #+clj [clojure.edn :as edn]
            #+clj [routes.core :refer [defroutes]]
            #+clj [clojure.test :refer :all]
            #+cljs [cemerick.cljs.test :as t]))

(def server
  {:scheme :http
   :server-name "example.com"
   :server-port 80})

(defroutes my-routes
  [{:route-name :continents,
    :path-re #"/continents",
    :method :get,
    :path "/continents",
    :path-parts ["" "continents"],
    :path-params []}
   {:route-name :continent,
    :path-re #"/continents/([^/]+)",
    :method :get,
    :path-constraints {:id "([^/]+)"},
    :path "/continents/:id",
    :path-parts ["" "continents" :id],
    :path-params [:id]}
   {:route-name :create-continent,
    :path-re #"/continents",
    :method :post,
    :path "/continents",
    :path-parts ["" "continents"],
    :path-params []}
   {:route-name :delete-continent,
    :path-re #"/continents/([^/]+)",
    :method :delete,
    :path-constraints {:id "([^/]+)"},
    :path "/continents/:id",
    :path-parts ["" "continents" :id],
    :path-params [:id]}
   {:route-name :update-continent,
    :path-re #"/continents/([^/]+)",
    :method :put,
    :path-constraints {:id "([^/]+)"},
    :path "/continents/:id",
    :path-parts ["" "continents" :id],
    :path-params [:id]}]
  {:scheme :http
   :server-name "example.com"
   :server-port 80})

(deftest test-assoc-route
  (let [routes (routes/assoc-route {} :continents #"/continents")
        route (:continents routes)]
    (is (= :get (:method route)))
    (is (= :continents (:route-name route)))
    #+clj (is (= "/continents" (str (:path-re route))))
    #+cljs (is (= "/\\/continents/" (str (:path-re route)))))
  (let [routes (routes/assoc-route {} :create-continent #"/continents" {:method :post})
        route (:create-continent routes)]
    (is (= :post (:method route)))
    (is (= :create-continent (:route-name route)))
    #+clj (is (= "/continents" (str (:path-re route))))
    #+cljs (is (= "/\\/continents/" (str (:path-re route))))))

(deftest test-expand-path
  (are [name opts expected]
    (is (= expected (routes/expand-path (get my-routes name) {:path-params opts})))
    :continents {} "/continents"
    :continent {:id 1} "/continents/1"
    :create-continent {} "/continents"
    :delete-continent {:id 1} "/continents/1"
    :update-continent {:id 1} "/continents/1"))

(deftest test-resolve-route-empty-params
  (let [request (routes/resolve-route my-routes :continent {})]
    (is (= :get (:method request)))
    (is (= :http (:scheme request)))
    (is (= "example.com" (:server-name request)))
    (is (= 80 (:server-port request)))
    (is (= "/continents/:id" (:uri request)))))

(deftest test-resolve-route-not-existing
  (is (nil? (routes/resolve-route my-routes :not-existing)))
  (let [request {:method :get :url "http://example.com"}]
    (is (= request (routes/resolve-route my-routes :not-existing request)))))

(deftest test-resolve-route-without-routes
  (is (nil? (routes/resolve-route nil)))
  (let [request {:method :get :url "http://example.com"}]
    (is (= request (routes/resolve-route request)))))

(deftest test-resolve-route-with-request
  (let [request {:method :get :url "http://example.com"}]
    (is (= request (routes/resolve-route my-routes request)))))

(deftest test-resolve-route-continent
  (let [request (routes/resolve-route my-routes :continent {:path-params {:id 1}})]
    (is (= :get (:method request)))
    (is (= :http (:scheme request)))
    (is (= "example.com" (:server-name request)))
    (is (= 80 (:server-port request)))
    (is (= "/continents/1" (:uri request)))))

(deftest test-resolve-route-continents
  (let [request (routes/resolve-route my-routes :continents)]
    (is (= :get (:method request)))
    (is (= :http (:scheme request)))
    (is (= "example.com" (:server-name request)))
    (is (= 80 (:server-port request)))
    (is (= "/continents" (:uri request)))))

(deftest test-resolve-route-create-continent
  (let [request (routes/resolve-route my-routes :create-continent {:edn-body {:id 1 :name "Europe"}})]
    (is (= {:id 1 :name "Europe"} (:edn-body request)))
    (is (= :post (:method request)))
    (is (= :http (:scheme request)))
    (is (= "example.com" (:server-name request)))
    (is (= 80 (:server-port request)))
    (is (= "/continents" (:uri request)))))

(deftest test-resolve-route-delete-continent
  (let [request (routes/resolve-route my-routes :delete-continent {:path-params {:id 1}})]
    (is (= :delete (:method request)))
    (is (= :http (:scheme request)))
    (is (= "example.com" (:server-name request)))
    (is (= 80 (:server-port request)))
    (is (= "/continents/1" (:uri request)))))

(deftest test-resolve-route-update-continent
  (let [request (routes/resolve-route my-routes :update-continent {:edn-body {:id 1 :name "Europe"}})]
    (is (= {:id 1 :name "Europe"} (:edn-body request)))
    (is (= :put (:method request)))
    (is (= :http (:scheme request)))
    (is (= "example.com" (:server-name request)))
    (is (= 80 (:server-port request)))
    (is (= "/continents/1" (:uri request)))))

(deftest test-resolve-route-override-defaults
  (let [default {:scheme :https :server-name "other.com" :server-port 8080}
        request (routes/resolve-route my-routes :continents default)]
    (is (= :get (:method request)))
    (is (= (:scheme default) (:scheme request)))
    (is (= (:server-name default) (:server-name request)))
    (is (= (:server-port default) (:server-port request)))
    (is (= "/continents" (:uri request)))))

(deftest test-path-for-routes
  (is (nil? (routes/path-for-routes my-routes nil)))
  (is (nil? (routes/path-for-routes my-routes :not-existing)))
  (are [name opts expected]
    (is (= expected (routes/path-for-routes my-routes name opts)))
    :continents {} "/continents"
    :continents {:query-params {:a 1 :b 2}} "/continents?a=1&b=2"
    :continent {} "/continents/:id"
    :continent {:id 1} "/continents/1"
    :continent {:path-params {:id 1}} "/continents/1"
    :continent {:path-params {:id 1} :query-params {:a 1}} "/continents/1?a=1"
    :create-continent {} "/continents"
    :delete-continent {:id 1} "/continents/1"
    :delete-continent {:path-params {:id 1}} "/continents/1"
    :update-continent {:id 1} "/continents/1"
    :update-continent {:path-params {:id 1}} "/continents/1"))

(deftest test-url-for-routes
  (is (nil? (url-for nil nil)))
  (is (nil? (url-for {} nil)))
  (is (nil? (url-for nil :not-existing)))
  (is (nil? (url-for {} :not-existing)))
  (let [server {:scheme :http
                :server-name "other.com"
                :server-port 80}]
    (are [server name opts expected]
      (is (= expected (url-for server name opts)))
      nil :continents {} "http://example.com/continents"
      server :continents {} "http://other.com/continents"
      server :continent {} "http://other.com/continents/:id"
      server :continent {:id 1} "http://other.com/continents/1"
      server :continent {:path-params {:id 1}} "http://other.com/continents/1"
      server :continent {:path-params {:id 1} :query-params {:a 1}} "http://other.com/continents/1?a=1"
      server :create-continent {} "http://other.com/continents"
      server :delete-continent {:id 1} "http://other.com/continents/1"
      server :delete-continent {:path-params {:id 1}} "http://other.com/continents/1"
      server :update-continent {:id 1} "http://other.com/continents/1"
      server :update-continent {:path-params {:id 1}} "http://other.com/continents/1"
      server :continents {:server-port 80} "http://other.com/continents"
      server :continents {:server-port 8080} "http://other.com:8080/continents"
      server :continents {:scheme :https :server-port 443} "https://other.com/continents"
      nil :continents {:scheme :https :server-port 8080} "https://example.com:8080/continents"
      server :continents {:scheme :https :server-port 8080} "https://other.com:8080/continents")))

#+clj
(deftest test-read-routes
  (let [routes (routes/read-routes "test-resources/routes.edn")]
    (is (not (empty? routes)))
    (let [route (:spots routes)]
      (is (= :spots (:route-name route)))
      (is (= :get (:method route)))
      (is (= "/spots" (:path route)))
      (is (= [] (:path-params route))))))

#+clj
(deftest test-spit-routes
  (let [old (routes/read-routes "test-resources/routes.edn")
        filename "/tmp/test-spit-routes"]
    (routes/spit-routes filename old)
    (let [new (routes/read-routes filename)]
      (is (= (set (keys old)) (set (keys new))))
      (is (= (map routes/serialize-route (vals old))
             (map routes/serialize-route (vals new)))))))

(deftest test-path-matches
  (let [route (first (routes/path-matches my-routes "/continents/1"))]
    (is (= "/continents/1" (:uri route)))
    (is (= :continent (:route-name route)))
    (is (= "/continents/:id" (:path route)))
    (is (= {:id "1"} (:path-params route)))))



(defroutes my-routes
  [{:route-name :countries,
    :path-re #"/countries",
    :method :get,
    :path "/countries",
    :path-parts ["" "countries"],
    :path-params []}
   {:route-name :country,
    :path-re #"/countries/([^/]+)",
    :method :get,
    :path-constraints {:id "([^/]+)"},
    :path "/countries/:id",
    :path-parts ["" "countries" :id],
    :path-params [:id]}]
  {:scheme :http
   :server-name "example.com"
   :server-port 80})


(path-for :countries)
(path-for :country {:path-params {:id 1}})

(def server
  {:scheme :https
   :server-name "example.com"
   :server-port 443})
