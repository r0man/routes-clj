(ns routes.test.server
  (:require [routes.server :refer [server-url example-server]]))

(defn test-server-url []
  (assert (= "https://example.com" (server-url {:server-name "example.com"})))
  (assert (= "https://example.com" (server-url example-server)))
  (assert (= "http://example.com" (server-url (assoc example-server :scheme :http)))))

(defn test []
  (test-server-url))
