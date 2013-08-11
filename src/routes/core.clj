(ns routes.core
  (:require [routes.platform :as platform]))

(defmacro defroutes [name routes & {:as opts}]
  `(def ~name
     ~(zipmap (map :route-name routes)
              (map (partial merge opts) routes))))

(def request platform/request)
