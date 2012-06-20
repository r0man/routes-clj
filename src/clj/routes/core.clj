(ns routes.core
  (:refer-clojure :exclude (replace))
  (:use [clojure.string :only (upper-case replace)]
        [routes.util :only (parse-keys)]))

(defmacro defroute [name args pattern]
  (let [name# name args# args pattern# pattern]
    `(do (routes.routes/register
          (routes.routes/map->Route
           {:name ~(keyword name#)
            :args (quote ~args#)
            :pattern ~pattern#
            :params ~(parse-keys pattern#)}))
         (defn ^:export ~(symbol (str name# "-route")) []
           (routes.routes/route ~(keyword name#)))
         (defn ^:export ~(symbol (str name# "-path")) [~@args#]
           (routes.util/format-pattern ~pattern# ~@args#))
         (defn ^:export ~(symbol (str name# "-url")) [~@args#]
           (str (routes.util/server-url)
                (routes.util/format-pattern ~pattern# ~@args#))))))
