(ns routes.test.server
  (:require [routes.server :refer [server-url example]]))

(defn test-server-url []
  (assert (= "https://example.com" "https://example.com"))
  (assert (= "https://example.com" (server-url {:server-name "example.com"})))
  (assert (= "http://example.com" (server-url {:scheme :http :server-name "example.com" :server-port 80})))
  (assert (= "https://example.com" (server-url {:scheme :https :server-name "example.com" :server-port 443})))
  (assert (= "https://example.com" (server-url example)))
  (assert (= "http://example.com" (server-url (assoc example :scheme :http)))))

(defn test []
  (test-server-url))
