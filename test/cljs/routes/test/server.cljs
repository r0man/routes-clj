(ns routes.test.server
  (:require [routes.server :refer [server-url example]]))

(defn test-server-url []
  (assert (= "https://example.com" (server-url {:server-name "example.com"})))
  (assert (= "https://example.com" (server-url example)))
  (assert (= "http://example.com" (server-url (assoc example :scheme :http)))))

(defn test []
  (test-server-url))
