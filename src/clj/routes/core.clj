(ns routes.core
  (:require [routes.helper :refer [route route-args route-path route-url route-symbol route-server]]
            [routes.helper :refer [map->Route make-route make-params parse-pattern register qualified?]]
            [routes.server :refer [*server*]]))

(defn- qualify [s]
  (symbol (str (if-not (qualified? s) (str *ns* "/")) s)))

(defmacro defroute
  "Define a route."
  [name args [pattern & params] & {:as options}]
  (let [name# name
        args# args
        pattern# pattern
        params# (apply vector params)
        options# options
        symbol# (symbol (str name# "-route"))
        route# (make-route
                (str *ns*) symbol# args# pattern# params#
                (assoc options# :root (route (qualify (:root options#)))))]
    `(do
       (def ^:export ~symbol#
         (make-route ~(str *ns*) ~(str symbol#) (quote ~args#) ~pattern# ~params# ~options#))
       (defn ^:export ~(symbol (str name# "-path")) [~@(route-args route#)]
         (route-path ~symbol# ~@(route-args route#)))
       (defn ^:export ~(symbol (str name# "-url")) [~@(route-args route#)]
         (route-url ~symbol# ~@(route-args route#)))
       (defn ^:export ~(symbol (str name# "-request")) [~@(route-args route#)]
         (let [server# (route-server ~symbol#)]
           {:scheme (:scheme server#)
            :server-name (:server-name server#)
            :server-port (:server-port server#)
            :uri (route-path ~symbol# ~@(route-args route#))
            :request-method :get})))))

(defmacro defparam [name doc & [format-fn parse-fn]]
  (let [name# name]
    `(def ~name#
       (routes.params/->Parameter
        ~(str name#)
        ~doc
        (or ~format-fn str)
        (or ~parse-fn identity)))))

(defmacro with-server [server & body]
  "Evaluate `body` with *server* bound to `server`."
  `(binding [routes.server/*server*
             (routes.helper/parse-url ~server)]
     ~@body))
