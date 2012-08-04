(ns routes.test.helper
  (:use clojure.test
        routes.helper
        routes.test.core))

(deftest test-format-path
  (are [r args expected]
    (is (= expected (apply format-path r args)))
    (route :continents) []
    "/continents"
    (route :continent) [europe]
    "/continents/eu-europe"
    (route :country-of-continent) [europe spain]
    "/continents/eu-europe/countries/es-spain"))

(deftest test-format-url
  (are [r args expected]
    (is (= expected (apply format-url r args)))
    (route :continents) []
    "https://example.com/continents"
    (route :continent) [europe]
    "https://example.com/continents/eu-europe"
    (route :country-of-continent) [europe spain]
    "https://example.com/continents/eu-europe/countries/es-spain"))

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
    (is (= example-route (route (:name example-route))))))

(deftest test-path
  (are [segments expected]
    (is (= expected (apply path segments)))
    ["continents"]
    "/continents"
    ["/continents"]
    "/continents"
    ["//continents"]
    "/continents"
    ["continents" "eu-europe"]
    "/continents/eu-europe"
    ["/continents" "/eu-europe"]
    "/continents/eu-europe"))

(deftest test-parse-keys
  (are [pattern expected]
    (is (= expected (parse-keys pattern)))
    "/continents/[:iso-3166-1-alpha-2]-[:name]"
    [[[:iso-3166-1-alpha-2] [:name]]]
    "/addresses/[:location :latitude],[:location :longitude]"
    [[[:location :latitude] [:location :longitude]]]))

(deftest test-parse-pattern
  (are [pattern expected]
    (is (= expected (parse-pattern pattern)))
    nil
    ""
    ""
    ""
    "/continents"
    "/continents"
    "/continents/[:iso-3166-1-alpha-2]-[:name]"
    "/continents/%s-%s"
    "/addresses/[:location :latitude],[:location :longitude]"
    "/addresses/%s,%s"))

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