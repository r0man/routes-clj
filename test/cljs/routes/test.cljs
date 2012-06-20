(ns routes.test
  (:require [routes.test.core :as core]
            [routes.test.helper :as helper]))

(defn ^:export run []
  (core/test)
  (helper/test)
  "All tests passed.")