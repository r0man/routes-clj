(ns routes.core
  (:require [routes.helper :refer [route make-params parse-pattern make-route route-args register]]
            [routes.server :refer [*server*]]))

(defmacro defroute
  "Define a route."
  [name args pattern & {:as options}]
  (let [name# name
        args# args
        pattern# pattern
        options# options
        symbol# (symbol (str name# "-route"))
        route# (register (make-route symbol# args pattern options))]
    `(do
       (def ^:export ~symbol#
         (routes.helper/register
          (routes.helper/map->Route
           {:ns (quote ~(symbol (str *ns*)))
            :name (quote ~symbol#)
            :qualified (quote ~(symbol (str *ns* "/" symbol#)))
            :root ~(:root options)
            :args (quote ~args)
            :pattern ~(parse-pattern (first pattern))
            :params ~(apply make-params pattern)
            :server (or ~(:server options#) (:server ~(:root options#)))})))
       (defn ^:export ~(symbol (str name# "-path")) [~@(route-args route#)]
         (routes.helper/format-path ~symbol# ~@(route-args route#)))
       (defn ^:export ~(symbol (str name# "-url")) [~@(route-args route#)]
         (routes.helper/format-url ~symbol# ~@(route-args route#))))))

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
