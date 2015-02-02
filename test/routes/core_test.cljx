(ns routes.core-test
  #+cljs (:require-macros [cemerick.cljs.test :refer [deftest is are]]
                          [routes.core :refer [defroutes]])
  (:require [no.en.core :refer [parse-url]]
            [routes.core :as routes]
            #+clj [routes.core :refer [defroutes]]
            #+clj [clojure.test :refer :all]
            #+cljs [cemerick.cljs.test :as t]))

(def server
  {:scheme :http
   :server-name "example.com"
   :server-port 80})

(def spain
  {:id 1 :iso-3166-1-alpha-2 "ES" :name "Spain"})

(def mundaka
  {:id 2 :name "Mundaka"})

(defroutes my-routes
  ["/countries" :countries]
  ["/countries/:id-:name" :country]
  ["/countries/:id-:name/spots" :spots-in-country]
  ["/countries/:id-:name/spots/:id-:name" :spot-in-country]
  ["/spots" :spots]
  ["/spots/:id-:name" :spot])

(defn- request
  ([method url]
   (request method url nil))
  ([method url params]
   (let [url (if (re-matches #"https?://" url) url (str "http://localhost" url))]
     (assoc (parse-url url)
            :request-method (keyword method)))))

(deftest test-find-route
  (is (nil? (routes/find-route my-routes :unknown)))
  (let [route (routes/find-route my-routes :countries)]
    (is (= (:name route) :countries))
    (is (= (:path-params route) [[] []]))
    (is (= (:path-parts route) ["" "countries"]))))

(deftest test-split-args
  (let [opts {:sort "asc"}]
    (are [route args expected]
      (= (routes/split-args (routes/find-route my-routes route) args)
         expected)
      :countries [] [[] []]
      :countries [opts] [[] [opts]]
      :country [spain] [[spain] []]
      :country [spain opts] [[spain] [opts]]
      :spots-in-country [spain] [[spain] []]
      :spots-in-country [spain opts] [[spain] [opts]]
      :spot-in-country [spain mundaka opts] [[spain mundaka] [opts]])))

(deftest test-path-for
  (are [route args expected]
    (= (apply routes/path-for my-routes route args) expected)
    :countries [] "/countries"
    :country [spain] "/countries/1-Spain"
    :spots-in-country [spain] "/countries/1-Spain/spots"
    :spot-in-country [spain mundaka] "/countries/1-Spain/spots/2-Mundaka"
    :spots [] "/spots"
    :spot [mundaka] "/spots/2-Mundaka"
    :spots [{:sort "asc"}] "/spots?sort=asc"))

(deftest test-url-for
  (are [route args expected]
    (= (apply routes/url-for my-routes server route args) expected)
    :countries [] "http://example.com/countries"
    :country [spain] "http://example.com/countries/1-Spain"
    :spots-in-country [spain] "http://example.com/countries/1-Spain/spots"
    :spot-in-country [spain mundaka] "http://example.com/countries/1-Spain/spots/2-Mundaka"
    :spots [] "http://example.com/spots"
    :spot [mundaka] "http://example.com/spots/2-Mundaka"
    :spots [{:sort "asc"}] "http://example.com/spots?sort=asc"))

(deftest test-request-for
  (are [route args expected]
    (= (apply routes/request-for my-routes server route args) expected)
    :countries []
    {:scheme :http
     :server-name "example.com"
     :server-port 80
     :request-method :get
     :uri "/countries"}
    :countries [{:query-params {:sort "asc"}}]
    {:scheme :http
     :server-name "example.com"
     :server-port 80
     :request-method :get
     :uri "/countries"
     :query-params {:sort "asc"}}
    :country [spain]
    {:request-method :get
     :scheme :http,
     :server-name "example.com",
     :server-port 80,
     :uri "/countries/1-Spain",}
    :spots-in-country [spain]
    {:request-method :get
     :scheme :http
     :server-name "example.com"
     :server-port 80
     :uri "/countries/1-Spain/spots"}
    :spot-in-country [spain mundaka]
    {:request-method :get
     :scheme :http
     :server-name "example.com"
     :server-port 80
     :uri "/countries/1-Spain/spots/2-Mundaka"}
    :spots []
    {:request-method :get
     :scheme :http
     :server-name "example.com"
     :server-port 80
     :uri "/spots"}))

(deftest test-route-compile
  (are [route expected]
    (let [compiled (routes/route-compile route)]
      (is (= (:path-params compiled)
             (:path-params expected)))
      (is (= (:path-parts compiled)
             (:path-parts expected)))
      (is (= (str (:path-re compiled))
             (str (:path-re expected)))))
    nil nil
    "" nil

    "/"
    {:path-params []
     :path-parts []
     :path-re
     #+clj "/"
     #+cljs "/\\//"}

    "/:id"
    {:path-params [[] [:id]]
     :path-parts ["" ":id"]
     :path-re
     #+clj "/([^/]+)"
     #+cljs "//([^/]+)/"}

    "/:id-:iso-3166-1-alpha-2"
    {:path-params [[] [:id :iso-3166-1-alpha-2]]
     :path-parts ["" ":id-:iso-3166-1-alpha-2"]
     :path-re
     #+clj "/([^/]+)-([^/]+)"
     #+cljs "//([^/]+)-([^/]+)/"}

    "/countries"
    {:path-params [[] []]
     :path-parts ["" "countries"]
     :path-re
     #+clj "/countries"
     #+cljs "//countries/"}

    "/countries/:id"
    {:path-params [[] [] [:id]]
     :path-parts ["" "countries" ":id"]
     :path-re
     #+clj "/countries/([^/]+)"
     #+cljs "//countries/([^/]+)/"}

    "/countries/:id/spots"
    {:path-params [[] [] [:id] []]
     :path-parts ["" "countries" ":id" "spots"]
     :path-re
     #+clj "/countries/([^/]+)/spots"
     #+cljs "//countries/([^/]+)/spots/"}))

(deftest test-fixed-path
  (are [path]
    (routes/route-matches path (request :get path))
    "/"
    "/foo"
    "/foo/bar"
    "/foo/bar.html"))

(deftest test-keyword-paths
  (are [path uri params]
    (= (:params (routes/route-matches path (request :get uri))) params)
    "/:x"      "/foo"     {:x "foo"}
    "/foo/:x"  "/foo/bar" {:x "bar"}
    "/a/b/:c"  "/a/b/c"   {:c "c"}
    "/:a/b/:c" "/a/b/c"   {:a "a", :c "c"}))

;; ;; (deftest test-keywords-match-extensions
;; ;;   (are [path uri params]
;; ;;     (= (:params (routes/route-matches path (request :get uri))) params)
;; ;;     "/foo.:ext" "/foo.txt" {:ext "txt"}
;; ;;     "/:x.:y"    "/foo.txt" {:x "foo", :y "txt"}))

(deftest test-hyphen-keywords
  (are [path uri params]
    (= (:params (routes/route-matches path (request :get uri))) params)
    "/:foo-bar" "/baz" {:foo-bar "baz"}
    "/:foo-"    "/baz" {:foo- "baz"}))

(deftest test-underscore-keywords
  (are [path uri params]
    (= (:params (routes/route-matches path (request :get uri)))
       params)
    "/:foo_bar" "/baz" {:foo_bar "baz"}
    "/:_foo"    "/baz" {:_foo "baz"}))

(deftest test-urlencoded-keywords
  (are [path uri params]
    (= (:params (routes/route-matches path (request :get uri))) params)
    "/:x" "/foo%20bar" {:x "foo%20bar"}
    "/:x" "/foo+bar"   {:x "foo+bar"}
    "/:x" "/foo%5Cbar" {:x "foo%5Cbar"}))

(deftest test-same-keyword-many-times
  (are [path uri params]
    (= (:params (routes/route-matches path (request :get uri))) params)
    "/:x/:x/:x" "/a/b/c" {:x ["a" "b" "c"]}
    "/:x/b/:x"  "/a/b/c" {:x ["a" "c"]}))

(deftest test-non-ascii-keywords
  (are [path uri params]
    (= (:params (routes/route-matches path (request :get uri))) params)
    "/:äñßOÔ"   "/abc"     {:äñßOÔ "abc"}
    "/:ÁäñßOÔ"  "/abc"     {:ÁäñßOÔ "abc"}
    "/:ä/:ش"   "/foo/bar" {:ä "foo" :ش "bar"}
    "/:ä/:ä"    "/foo/bar" {:ä ["foo" "bar"]}
    "/:Ä-ü"     "/baz"     {:Ä-ü "baz"}
    "/:Ä_ü"     "/baz"     {:Ä_ü "baz"}))

;; ;; (deftest test-wildcard-paths
;; ;;   (are [path uri params]
;; ;;     (= (:params (routes/route-matches path (request :get uri))) params)
;; ;;        "/*"     "/foo"         {:* "foo"}
;; ;;        "/*"     "/foo.txt"     {:* "foo.txt"}
;; ;;        "/*"     "/foo/bar"     {:* "foo/bar"}
;; ;;        "/foo/*" "/foo/bar/baz" {:* "bar/baz"}
;; ;;        "/a/*/d" "/a/b/c/d"     {:* "b/c"}))

(deftest test-escaped-chars
  (are [path uri params]
    (= (:params (routes/route-matches path (request :get uri))) params)
    "/\\:foo" "/foo"  nil
    ;; "/\\:foo" "/:foo" {}
    ))

(deftest test-inline-regexes
  (are [path uri params]
    (= (:params (routes/route-matches path (request :get uri))) params)
    "/:x{\\d+}"   "/foo" nil
    "/:x{\\d+}"   "/10"  {:x "10"}
    "/:x{\\d{2}}" "/2"   nil
    "/:x{\\d{2}}" "/20"  {:x "20"}
    "/:x{\\d}/b"  "/3/b" {:x "3"}
    "/:x{\\d}/b"  "/a/b" nil
    "/a/:x{\\d}"  "/a/4" {:x "4"}
    "/a/:x{\\d}"  "/a/b" nil))

(deftest test-compiled-routes
  (is (= (:params (routes/route-matches
                   (routes/route-compile "/foo/:id")
                   (request :get "/foo/bar")))
         {:id "bar"})))

;; ;; (deftest test-url-paths
;; ;;   (is (routes/route-matches
;; ;;        "http://localhost/"
;; ;;        {:scheme  :http
;; ;;         :headers {"host" "localhost"}
;; ;;         :uri     "/"}))
;; ;;   (is (routes/route-matches
;; ;;        "//localhost/"
;; ;;        {:scheme  :http
;; ;;         :headers {"host" "localhost"}
;; ;;         :uri     "/"}))
;; ;;   (is (routes/route-matches
;; ;;        "//localhost/"
;; ;;        {:scheme  :https
;; ;;         :headers {"host" "localhost"}
;; ;;         :uri     "/"})))

;; ;; (deftest test-url-port-paths
;; ;;   (let [req (request :get "http://localhost:8080/")]
;; ;;     (is (routes/route-matches "http://localhost:8080/" req))
;; ;;     (is (not (routes/route-matches "http://localhost:7070/" req)))))

(deftest test-unmatched-paths
  (is (nil? (routes/route-matches "/foo" (request :get "/bar")))))

(deftest test-path-info-matches
  (is (routes/route-matches
       "/bar" (-> (request :get "/foo/bar")
                  (assoc :path-info "/bar")))))

(deftest test-custom-matches
  (let [route (routes/route-compile "/foo/:bar" {:bar #"\d+"})]
    (is (not (routes/route-matches route (request :get "/foo/bar"))))
    (is (not (routes/route-matches route (request :get "/foo/1x"))))
    (is (routes/route-matches route (request :get "/foo/10")))))

(deftest test-unused-regex-keys
  (is (thrown? #+clj clojure.lang.ExceptionInfo
               #+cljs js/Error
               (routes/route-compile "/:foo" {:foa #"\d+"})))
  (is (thrown? #+clj clojure.lang.ExceptionInfo
               #+cljs js/Error
               (routes/route-compile "/:foo" {:foo #"\d+" :bar #".*"}))))

;; ;; (deftest test-invalid-inline-patterns
;; ;;   (is (thrown? ExceptionInfo (route-compile "/:foo{")))
;; ;;   (is (thrown? ExceptionInfo (route-compile "/:foo{\\d{2}")))
;; ;;   (is (thrown? PatternSyntaxException (route-compile "/:foo{[a-z}"))))
