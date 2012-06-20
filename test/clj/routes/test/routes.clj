(ns routes.test.routes
  (:use clojure.test
        routes.routes))

(deftest test-routes
  (is (nil? (route :unknown-route))))

(deftest test-register
  (let [example-route {:name :example-route}]
    (register example-route)
    (is (= example-route (get @*routes* (:name example-route))))))
