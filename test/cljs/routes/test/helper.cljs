(ns routes.test.helper
  (:require [routes.helper :refer [link-to map->Route route register parse-url]]))

(defn test-link-to []
  (assert (= (link-to "http://example.com/")
             [:a {:href "http://example.com/"} nil]))
  (assert (= (link-to "http://example.com/" "foo")
             [:a {:href "http://example.com/"} (list "foo")]))
  (assert (= (link-to "http://example.com/" "foo" "bar")
             [:a {:href "http://example.com/"} (list "foo" "bar")])))

(defn test-route []
  (assert (nil? (route :unknown-route))))

(defn test-register-route []
  (let [example (map->Route {:name "example"})]
    (register example)
    (assert (= example (route (:name example))))))

(defn test-parse-url []
  (assert (nil? (parse-url nil)))
  (assert (nil? (parse-url "")))
  (assert (= {:scheme :https :server-name "example.com" :server-port 443 :uri "/"}
             (parse-url "example.com")))
  (assert (= {:scheme :https :server-name "example.com" :server-port 81 :uri "/"}
             (parse-url "example.com:81")))
  (assert (= {:scheme :http :server-name "example.com" :server-port 81 :uri "/continents"}
             (parse-url "http://example.com:81/continents")))
  (let [url "http://example.com/continents"]
    (assert (= (parse-url url) (parse-url (parse-url url))))))

(defn test []
  (test-link-to)
  (test-route)
  (test-register-route)
  (test-parse-url))