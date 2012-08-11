(ns routes.core
  (:require [routes.helper :refer [make-route route route-args route-symbol]]
            [routes.helper :refer [register qualified? make-params parse-pattern]]
            [routes.server :refer [*server*]]))

(defn- qualify [s]
  (symbol (str (if-not (qualified? s) (str *ns* "/")) s)))

(defmacro defroute
  "Define a route."
  [name args pattern & {:as options}]
  (let [name# name
        args# args
        pattern# pattern
        options# (assoc options :root (route (qualify (:root options))))
        symbol# (symbol (str name# "-route"))
        route# (register (make-route (str *ns*) symbol# args pattern options#))]
    `(do
       (def ^:export ~symbol#
         (routes.helper/register
          (routes.helper/map->Route
           {:ns (quote ~(symbol (str *ns*)))
            :name (quote ~symbol#)
            :root ~(route-symbol (:root route#))
            :args (quote ~args)
            :pattern ~(parse-pattern (first pattern))
            :params ~(apply make-params pattern)
            :server (or ~(:server options#) (:server ~(:root options#)))})))
       (defn ^:export ~(symbol (str name# "-path")) [~@(route-args route#)]
         (routes.helper/route-path ~symbol# ~@(route-args route#)))
       (defn ^:export ~(symbol (str name# "-url")) [~@(route-args route#)]
         (routes.helper/route-url ~symbol# ~@(route-args route#))))))

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
