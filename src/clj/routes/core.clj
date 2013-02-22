(ns routes.core
  (:require [routes.helper :refer [route route-args route-path route-url route-symbol route-server]]
            [routes.helper :refer [map->Route make-route make-params parse-pattern register qualified?]]))

(defn qualify [s]
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
        route# (register (make-route
                          (str *ns*) symbol# args# pattern# params#
                          (assoc options# :root (route (qualify (:root options#))))))]
    `(do
       (defn ^:export ~symbol# []
         (make-route ~(str *ns*) ~(str symbol#) (quote ~args#) ~pattern# ~params# ~options#))
       (defn ^:export ~(symbol (str name# "-path")) [~@(route-args route#) & ~'params]
         (apply route-path (~symbol#) [~@(route-args route#)] ~'params))
       (defn ^:export ~(symbol (str name# "-url")) [~@(route-args route#) & ~'params]
         (apply route-url (~symbol#) [~@(route-args route#)] ~'params))
       (defn ^:export ~(symbol (str name# "-request")) [~@(route-args route#) & {:as ~'params}]
         (let [~'server (route-server (~symbol#))]
           {:scheme (:scheme ~'server)
            :server-name (:server-name ~'server)
            :server-port (:server-port ~'server)
            :uri (route-path (~symbol#) [~@(route-args route#)])
            :request-method :get
            :query-params ~'params})))))

(defmacro defparam [name doc & [format-fn parse-fn]]
  (let [name# name]
    `(def ~name#
       (routes.params/->Parameter
        ~(str name#)
        ~doc
        (or ~format-fn str)
        (or ~parse-fn identity)))))
