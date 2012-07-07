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
  (assert (= {:scheme :https :server-name "api.burningswell.com" :server-port nil :uri nil}
             (parse-url "api.burningswell.com")))
  (assert (= {:scheme :https :server-name "api.burningswell.com" :server-port 80 :uri nil}
             (parse-url "api.burningswell.com:80")))
  (assert (= {:scheme :http :server-name "api.burningswell.com" :server-port 81 :uri "/base"}
             (parse-url "http://api.burningswell.com:81/base"))))

(defn test []
  (test-link-to)
  (test-route)
  (test-register-route)
  (test-parse-url))