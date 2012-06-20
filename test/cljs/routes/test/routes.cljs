(ns routes.test.routes
  (:require [routes.routes :as routes]))

(defn test-route []
  (assert (nil? (routes/route :unknown-route))))

(defn test-register-route []
  (let [route (routes/map->Route {:name "x"})]
    (routes/register route)
    (assert (= route (routes/route (:name route))))))

(defn test []
  (test-route))