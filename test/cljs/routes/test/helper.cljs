(ns routes.test.helper
  (:require [routes.helper :as helper]))

(defn test-route []
  (assert (nil? (helper/route :unknown-route))))

(defn test-register-route []
  (let [route (helper/map->Route {:name "x"})]
    (helper/register route)
    (assert (= route (helper/route (:name route))))))

(defn test []
  (test-route))