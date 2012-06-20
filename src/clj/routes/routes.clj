(ns routes.routes)

(def ^:dynamic *routes* (atom {}))

(defrecord Route [name args pattern params])

(defn route
  "Lookup a route by `name`."
  [name] (get @*routes* (keyword name)))

(defn register
  "Register `route` by it's name."
  [route] (swap! *routes* assoc (:name route) route))
