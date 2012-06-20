(ns routes.test
  (:require [routes.test.core :as core]
            [routes.test.routes :as routes]
            [routes.test.util :as util]))

(defn ^:export run []
  (core/test)
  (routes/test)
  (util/test)
  "All tests passed.")