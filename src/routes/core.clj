(ns routes.core)

(defmacro defroutes [name routes & {:as opts}]
  `(def ~name
     ~(zipmap (map :route-name routes)
              (map (partial merge opts) routes))))
