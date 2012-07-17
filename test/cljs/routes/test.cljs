(ns routes.test
  (:require [routes.test.core :as core]
            [routes.test.helper :as helper]
            [routes.test.repl :as repl]
            [routes.test.server :as server]))

(defn ^:export run []
  (core/test)
  (helper/test)
  (repl/test)
  (server/test)
  "All tests passed.")