(ns routes.core
  (:refer-clojure :exclude (replace))
  (:require [clojure.string :refer [upper-case replace]]
            [routes.helper :refer [parse-keys parse-url]]
            [routes.server :refer [*server*]]))

(defmacro defroute
  "Define a route."
  [name args pattern]
  (let [name# name args# args pattern# pattern]
    `(let [server# routes.server/*server*]
       (routes.helper/register
        (routes.helper/map->Route
         {:name ~(keyword name#)
          :args (quote ~args#)
          :pattern ~pattern#
          :params ~(parse-keys pattern#)
          :server routes.server/*server*}))
       (defn ^:export ~(symbol (str name# "-route")) []
         (routes.helper/route ~(keyword name#)))
       (defn ^:export ~(symbol (str name# "-path")) [~@args#]
         (routes.helper/format-pattern ~pattern# ~@args#))
       (defn ^:export ~(symbol (str name# "-url")) [~@args#]
         (str (routes.server/server-url (or routes.server/*server* server#))
              (routes.helper/format-pattern ~pattern# ~@args#))))))

(defmacro with-server [server & body]
  "Evaluate `body` with *server* bound to `server`."
  (let [server (if (string? server) (parse-url server) server)]
    (binding [*server* server]
      `(binding [routes.server/*server* ~server]
         ~@body))))