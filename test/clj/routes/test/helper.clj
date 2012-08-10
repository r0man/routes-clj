(ns routes.test.helper
  (:use clojure.test
        routes.helper
        routes.test.fixtures))

(deftest test-format-path
  (are [r args expected]
    (is (= expected (apply format-path r args)))
    continents-route []
    "/continents"
    continent-route [europe]
    "/continents/1-europe"
    country-of-continent-route [europe spain]
    "/continents/1-europe/countries/es-spain"))

(deftest test-format-url
  (are [r args expected]
    (is (= expected (apply format-url r args)))
    continents-route []
    "https://example.com/continents"
    continent-route [europe]
    "https://example.com/continents/1-europe"
    country-of-continent-route [europe spain]
    "https://example.com/continents/1-europe/countries/es-spain"))

(deftest test-read-vector
  (are [string expected]
    (is (= expected (read-vector string)))
    nil []
    "" []
    "[:id]" [:id]
    "[:id :name]" [:id :name]))

(deftest link-to-test
  (is (= (link-to "http://example.com/")
         [:a {:href "http://example.com/"} nil]))
  (is (= (link-to "http://example.com/" "foo")
         [:a {:href "http://example.com/"} (list "foo")]))
  (is (= (link-to "http://example.com/" "foo" "bar")
         [:a {:href "http://example.com/"} (list "foo" "bar")])))

(deftest test-routes
  (is (nil? (route :unknown-route))))

(deftest test-register
  (let [example-route {:name :example-route}]
    (register example-route)
    (is (= example-route (route (str (:ns example-route) "/" (:name example-route)))))))

(deftest test-path
  (are [segments expected]
    (is (= expected (apply path segments)))
    ["/"]
    "/"
    ["continents"]
    "/continents"
    ["/" "/continents"]
    "/continents"
    ["continents" "1-europe"]
    "/continents/1-europe"
    ["/continents" "/1-europe"]
    "/continents/1-europe"))

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
    nil
    ""
    ""
    ""
    "/continents"
    "/continents"
    "/continents/:iso-3166-1-alpha-2-:name"
    "/continents/%s-%s"
    "/addresses/:location"
    "/addresses/%s"))

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
