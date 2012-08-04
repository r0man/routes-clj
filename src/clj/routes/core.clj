(ns routes.core
  (:require [routes.helper :refer [parse-keys parse-pattern]]
            [routes.server :refer [*server*]]))

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
           :pattern ~(parse-pattern pattern#)
           :params ~(parse-keys pattern#)
           :server (routes.helper/parse-url (or ~(:server options) routes.server/*server*))})))
       (defn ^:export ~(symbol (str name# "-route")) []
         (routes.helper/route ~(keyword name#)))
       (defn ^:export ~(symbol (str name# "-path")) [~@args#]
         (routes.helper/format-path
          (routes.helper/route ~(keyword name#))
          ~@args#))
       (defn ^:export ~(symbol (str name# "-url")) [~@args#]
         (routes.helper/format-url
          (routes.helper/route ~(keyword name#))
          ~@args#)))))

(defmacro with-server [server & body]
  "Evaluate `body` with *server* bound to `server`."
  `(binding [routes.server/*server* (routes.helper/parse-url ~server)]
     ~@body))
