(ns routes.core
  (:refer-clojure :exclude (replace))
  (:use [clojure.string :only [upper-case replace]]
        [routes.helper :only [parse-keys]]))

(defmacro defroute [name args pattern]
  (let [name# name args# args pattern# pattern]
    `(do (routes.helper/register
          (routes.helper/map->Route
           {:name ~(keyword name#)
            :args (quote ~args#)
            :pattern ~pattern#
            :params ~(parse-keys pattern#)}))
         (defn ^:export ~(symbol (str name# "-route")) []
           (routes.helper/route ~(keyword name#)))
         (defn ^:export ~(symbol (str name# "-path")) [~@args#]
           (routes.helper/format-pattern ~pattern# ~@args#))
         (defn ^:export ~(symbol (str name# "-url")) [~@args#]
           (str (routes.helper/server-url)
                (routes.helper/format-pattern ~pattern# ~@args#))))))

(defmacro with-server [server & body]
  `(binding [routes.helper/*server* ~server]
     ~@body))