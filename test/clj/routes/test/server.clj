(ns routes.test.server
  (:use clojure.test
        routes.server))

(def ^:dynamic *example*
  {:scheme :https :server-name "example.com" :server-port 443})

(deftest test-server-url
  (is (= "https://example.com" "https://example.com"))
  (is (= "https://example.com" (server-url {:server-name "example.com"})))
  (is (= "http://example.com" (server-url {:scheme :http :server-name "example.com" :server-port 80})))
  (is (= "https://example.com" (server-url {:scheme :https :server-name "example.com" :server-port 443})))
  (is (= "https://example.com" (server-url *example*)))
  (is (= "http://example.com:443" (server-url (assoc *example* :scheme :http)))))
