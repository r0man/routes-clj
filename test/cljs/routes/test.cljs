(ns routes.test
  (:require [routes.test.core :as core]))

(defn ^:export run []
  (core/test)
  "All tests passed.")