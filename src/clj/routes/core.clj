(ns routes.core
  (:refer-clojure :exclude (replace))
  (:require [clojure.string :refer [upper-case replace]]
            [routes.helper :refer [parse-keys parse-url]]
            [routes.server :refer [*server* parse-server]]))

(defmacro defroute
  "Define a route."
  [name args pattern & {:as options}]
  (let [name# name args# args pattern# pattern]
    `(do
       (routes.helper/register
        (routes.helper/map->Route
         (merge
          ~options
          {:name ~(keyword name#)
           :args (quote ~args#)
           :pattern ~pattern#
           :params ~(parse-keys pattern#)
           :server (routes.server/parse-server
                    (or ~(:server options)
                        routes.server/*server*))})))
       (defn ^:export ~(symbol (str name# "-route")) []
         (routes.helper/route ~(keyword name#)))
       (defn ^:export ~(symbol (str name# "-path")) [~@args#]
         (routes.helper/format-pattern ~pattern# ~@args#))
       (defn ^:export ~(symbol (str name# "-url")) [~@args#]
         (str (routes.server/server-url
               (or routes.server/*server*
                   (:server (routes.helper/route ~(keyword name#)))))
              (routes.helper/format-pattern ~pattern# ~@args#))))))

(defmacro with-server [server & body]
  "Evaluate `body` with *server* bound to `server`."
  `(binding [routes.server/*server* (routes.server/parse-server ~server)]
     ~@body))
