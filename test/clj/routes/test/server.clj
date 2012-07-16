(ns routes.test.server
  (:use clojure.test
        routes.server))

(deftest test-server-url
  (is (= "https://example.com" "https://example.com"))
  (is (= "https://example.com" (server-url {:server-name "example.com"})))
  (is (= "https://example.com" (server-url example)))
  (is (= "http://example.com" (server-url (assoc example :scheme :http)))))