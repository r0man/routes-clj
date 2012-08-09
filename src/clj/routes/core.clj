(ns routes.core
  (:require [routes.helper :refer [route path make-params parse-pattern]]
            [routes.server :refer [*server*]]))

(defmacro defroute
  "Define a route."
  [name args pattern & {:as options}]
  (let [name# name
        args# args
        pattern# pattern
        options# options
        root# (:root options)]
    `(do
       (def ^:export ~(symbol (str name# "-route"))
         (routes.helper/map->Route
          {:args (concat (:args ~root#) (quote ~args#))
           :name ~(str name#)
           :ns ~(str *ns*)
           :params (concat (:params ~root#) (apply make-params ~pattern#))
           :pattern (path (:pattern ~root#) ~(parse-pattern (first pattern#)))
           :root ~root#
           :server (or *server* ~(:server options#) (:server ~root#))}))
       (routes.helper/register ~(symbol (str name# "-route")))
       ;; TODO: Fixed args
       (defn ^:export ~(symbol (str name# "-path")) [& ~'args]
         (apply
          routes.helper/format-path
          ~(symbol (str name# "-route"))
          ~'args))
       ;; TODO: Fixed args
       (defn ^:export ~(symbol (str name# "-url")) [& ~'args]
         (apply
          routes.helper/format-url
          ~(symbol (str name# "-route"))
          ~'args)))))

(defmacro defparam [name doc & [format-fn parse-fn]]
  (let [name# name]
    `(def ~name#
       (routes.param/->Parameter
        ~(str name#)
        ~doc
        (or ~format-fn str)
        (or ~parse-fn identity)))))

(defmacro with-server [server & body]
  "Evaluate `body` with *server* bound to `server`."
  `(binding [routes.server/*server* (routes.helper/parse-url ~server)]
     ~@body))
