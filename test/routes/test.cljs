(ns routes.test
  (:require [doo.runner :refer-macros [doo-tests]]
            [routes.core-test]))

(doo-tests 'routes.core-test)
