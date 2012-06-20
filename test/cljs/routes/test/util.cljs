(ns routes.test.util
  (:require [routes.util :as util]))

(defn test-route []
  (assert (nil? (util/route :unknown-route))))

(defn test-register-route []
  (let [route (util/map->Route {:name "x"})]
    (util/register route)
    (assert (= route (util/route (:name route))))))

(defn test []
  (test-route))