(ns routes.test.helper
  (:require [routes.params :as params])
  (:use clojure.test
        routes.helper
        routes.server
        routes.test.core))

(deftest test-route-path
  (are [r args expected]
    (is (= expected (apply route-path r args)))
    (continents-route) []
    "/continents"
    (continent-route) [europe]
    "/continents/1-europe"
    (country-of-continent-1-route) [europe spain]
    "/continents/1-europe/countries/es-spain"
    (country-of-continent-2-route) [europe spain]
    "/continents/1-europe/countries/es-spain"))

(deftest test-route-url
  (are [r args expected]
    (is (= expected (apply route-url r args)))
    (continents-route) []
    "https://example.com/continents"
    (continent-route) [europe]
    "https://example.com/continents/1-europe"
    (country-of-continent-1-route) [europe spain]
    "https://example.com/continents/1-europe/countries/es-spain"
    (country-of-continent-2-route) [europe spain]
    "https://example.com/continents/1-europe/countries/es-spain"))

(deftest link-to-test
  (is (= (link-to "http://example.com/")
         [:a {:href "http://example.com/"} nil]))
  (is (= (link-to "http://example.com/" "foo")
         [:a {:href "http://example.com/"} (list "foo")]))
  (is (= (link-to "http://example.com/" "foo" "bar")
         [:a {:href "http://example.com/"} (list "foo" "bar")])))

(deftest test-route
  (is (nil? (route nil)))
  (is (nil? (route "")))
  (is (nil? (route 'unknown-route))))

(deftest test-route-symbol
  (is (nil? (route-symbol nil)))
  (is (= (symbol (str *ns* "/example-route"))
         (route-symbol {:ns *ns* :name 'example-route}))))

(deftest test-register
  (let [example-route {:ns *ns* :name 'example-route}]
    (register example-route)
    (is (= example-route (route (route-symbol example-route))))))

(deftest test-split-by
  (are [coll counts expected]
    (is (= expected (split-by coll counts)))
    ['a 'b 'c 'd] [1 1 1 1] [['a] ['b] ['c] ['d]]
    ['a 'b 'c 'd] [2 2] [['a 'b] ['c 'd]]))

(deftest test-path
  (are [segments expected]
    (is (= expected (apply path segments)))
    ["/"] "/"
    ["continents"] "/continents"
    ["/" "/continents"] "/continents"
    ["continents" "1-europe"] "/continents/1-europe"
    ["/continents" "/1-europe"] "/continents/1-europe"))

(deftest test-parse-keys
  (are [pattern expected]
    (is (= expected (parse-keys pattern)))
    "/continents/:iso-3166-1-alpha-2-:name"
    [[:iso-3166-1-alpha-2 :name]]
    "/continents/:iso-3166-1-alpha-2-:name/countries/:iso-3166-1-alpha-2-:name"
    [[:iso-3166-1-alpha-2 :name] [:iso-3166-1-alpha-2 :name]]
    "/addresses/:location"
    [[:location]]))

(deftest test-parse-pattern
  (are [pattern expected]
    (is (= expected (parse-pattern pattern)))
    nil ""
    "" ""
    "/continents" "/continents"
    "/continents/:iso-3166-1-alpha-2-:name" "/continents/%s-%s"
    "/continents/:id-:name/countries/:iso-3166-1-alpha-2-:name" "/continents/%s-%s/countries/%s-%s"
    "/addresses/:location" "/addresses/%s"))

(deftest test-parse-url
  (are [url expected]
    (is (= expected (parse-url url)))
    nil nil
    "" nil
    "example.com"
    {:scheme :https :server-name "example.com" :server-port 443 :uri "/"}
    "example.com:81"
    {:scheme :https :server-name "example.com" :server-port 81 :uri "/"}
    "http://example.com:81/continents"
    {:scheme :http :server-name "example.com" :server-port 81 :uri "/continents"}
    (parse-url "http://example.com:81/continents")
    {:scheme :http :server-name "example.com" :server-port 81 :uri "/continents"}))

;; (deftest test-make-route
;;   (let [root-route (make-route 'root-route '[] ["/"] :server example)]
;;     (is (= 'routes.test.helper (:ns root-route)))
;;     (is (= 'root-route (:name root-route)))
;;     (is (= 'routes.test.helper/root-route (:qualified root-route)))
;;     (is (nil? (:root root-route)))
;;     (is (= [] (:args root-route)))
;;     (is (= "/" (:pattern root-route)))
;;     (is (= [] (:params root-route)))
;;     (is (= example (:server root-route)))
;;     (let [continents-route (make-route 'continents-route '[] ["/continents"] :root root-route)]
;;       (is (= 'routes.test.helper (:ns continents-route)))
;;       (is (= 'continents-route (:name continents-route)))
;;       (is (= 'routes.test.helper/continents-route (:qualified continents-route)))
;;       (is (= root-route (:root continents-route)))
;;       (is (= [] (:args continents-route)))
;;       (is (= "/continents" (:pattern continents-route)))
;;       (is (= [] (:params continents-route)))
;;       (is (= example (:server continents-route)))
;;       (let [continent-route
;;             (make-route
;;              'continent-route
;;              '[continent]
;;              ["/:id-:name" params/integer params/string]
;;              :root continents-route)]
;;         (clojure.pprint/pprint continent-route)
;;         (is (= 'routes.test.helper (:ns continent-route)))
;;         (is (= 'continent-route (:name continent-route)))
;;         (is (= 'routes.test.helper/continent-route (:qualified continent-route)))
;;         (is (= continents-route (:root continent-route)))
;;         (is (= '[continent] (:args continent-route)))
;;         (is (= "/%s-%s" (:pattern continent-route)))
;;         (is (= [[(assoc params/integer :name "id")
;;                  (assoc params/string :name "name")]]
;;                (:params continent-route)))
;;         (is (= example (:server continent-route)))))))
