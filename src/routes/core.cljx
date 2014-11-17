(ns routes.core
  (:refer-clojure :exclude [replace])
  (:require [clojure.string :refer [blank? replace]]
            [no.en.core :refer [format-query-params format-url]]
            #+clj [clojure.pprint :refer [pprint]]
            #+clj [clojure.edn :as edn]))

(def route-keys
  [:method :route-name :path :path-params :path-re])

(defn assoc-route [routes route-name path-re & [opts]]
  (let [route (merge {:method :get} opts)
        route (assoc route :route-name route-name :path-re path-re)]
    (assoc routes route-name route)))

(defn- check-request [request]
  (if-not (or (:uri request) (:url request))
    (throw (ex-info "HTTP request is missing :uri or :url." {:request request}))
    request))

(defn find-route
  "Lookup the route `name` by keyword in `rou"
  [routes name]
  (get routes (keyword name)))

(defn expand-path
  "Format the `route` url by expanding :path-params in `opts`."
  [route & [opts]]
  (reduce
   (fn [uri param]
     (let [params (or (:path-params opts) (:edn-body opts) opts)]
       (if-let [value (-> params param)]
         (replace uri (str param) (str value))
         uri)))
   (:path route) (:path-params route)))

(defn resolve-route
  "Find the route `name` in `routes` and return the Ring request."
  ([request]
     request)
  ([routes request]
     (if (map? request)
       (resolve-route routes nil request)
       (resolve-route routes request nil)))
  ([routes name request]
     (if-let [route (find-route routes name)]
       (assoc (merge {:scheme :http :server-name "localhost"} route request)
         :uri (expand-path route request))
       request)))

(defn- match-path [path route]
  (if-let [matches (re-matches (:path-re route) path)]
    (assoc route
      :uri path
      :path-params (zipmap (:path-params route) (rest matches)))))

(defn path-matches
  [routes path & [method]]
  (let [method (or method :get)]
    (->> (vals routes)
         (filter #(= method (:method %1)))
         (map (partial match-path path))
         (remove nil?))))

(defn path-by-route
  "Find `route-name` in `routes` and return the path."
  [routes route-name & [opts]]
  (if (find-route routes route-name)
    (let [request (resolve-route routes route-name opts)
          query (format-query-params (:query-params opts))]
      (str (:uri request) (if-not (blank? query) (str "?" query))))))

(defn request-by-route
  "Find `route-name` in `routes` and return the request map."
  [routes server route-name & [opts]]
  (if (find-route routes route-name)
    (some-> (resolve-route routes route-name opts)
            (merge server opts)
            (update-in [:query-params] #(into (sorted-map) %)))))

(defn url-by-route
  "Find `route-name` in `routes` and return the url."
  [routes server route-name & [opts]]
  (some-> (request-by-route routes server route-name opts)
          (format-url)))

(defn strip-path-re [route]
  (update-in route [:path-re] #(if %1 (replace %1 #"\\Q|\\E" ""))))

(defn serialize-route [route]
  (update-in route [:path-re] #(if %1 (str %1))))

(defn deserialize-route [route]
  (update-in route [:path-re] #(if %1 (re-pattern %1))))

(defn zip-routes [routes & [opts]]
  (zipmap (map :route-name routes)
          (map #(merge opts %1) routes)))

(defmacro defroutes
  "Define routes."
  [name routes & {:as opts}]
  `(do (def ~name (routes.core/zip-routes ~routes ~opts))
       (defn ~'path-for [~'route-name & [~'opts]]
         (routes.core/path-by-route ~name ~'route-name ~'opts))
       (defn ~'request-for [~'server ~'route-name & [~'opts]]
         (routes.core/request-by-route ~name ~'server ~'route-name ~'opts))
       (defn ~'url-for [~'server ~'route-name & [~'opts]]
         (routes.core/url-by-route ~name ~'server ~'route-name ~'opts))))

#+clj
(defn read-routes
  "Read the routes in EDN format from `filename`."
  [filename]
  (->> (edn/read-string (slurp filename))
       (map deserialize-route)
       (zip-routes)))

#+clj
(defn spit-routes
  "Spit the `routes` in EDN format to `filename`."
  [filename routes]
  (spit filename
        (with-out-str
          (->> (vals routes)
               (map serialize-route)
               (sort-by :route-name)
               (pprint)))))
