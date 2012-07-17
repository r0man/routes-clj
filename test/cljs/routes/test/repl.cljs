(ns routes.test.repl
  (:require [routes.repl :refer [connect]]))

(defn test-connect []
  (connect))

(defn test []
  (test-connect))
