(ns routes.util
  (:refer-clojure :exclude [replace])
  (:require [clojure.string :refer [replace]]))

(defn format-uri
  "Format the `route` url by expanding :query-params in `opts`."
  [route & [opts]]
  (reduce
   (fn [uri param]
     (let [params (:query-params opts)]
       (if-let [value (-> params param)]
         (replace uri (str param) (str value))
         (throw (ex-info (format "Can't expand query param %s." param) params)))))
   (:path route) (:path-params route)))

(defn make-request
  "Find the route `name` in `routes` and return the Ring request."
  [routes name & [opts]]
  (when-let [route (get routes (keyword name))]
    (-> (merge route opts)
        (assoc :uri (format-uri route opts)))))
