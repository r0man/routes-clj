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
        params# params
        options# (assoc options :root (route (qualify (:root options))))
        symbol# (symbol (str name# "-route"))
        route# (register (make-route (str *ns*) symbol# args# pattern# params# options#))]
    `(do
       (def ^:export ~symbol#
         (register
          (map->Route
           {:ns (quote ~(symbol (str *ns*)))
            :name (quote ~symbol#)
            :root ~(route-symbol (:root route#))
            :args (quote ~args#)
            :pattern ~(parse-pattern pattern#)
            :params ~(apply make-params pattern# params#)
            :server (or ~(:server options#) (:server ~(:root options#)))})))
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
