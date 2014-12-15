(ns routes.core
  (:refer-clojure :exclude [replace])
  (:require [clojure.string :refer [blank? replace]]
            [no.en.core :refer [format-query-params format-url]]
            #+clj [clojure.pprint :refer [pprint]]
            #+clj [clojure.edn :as edn]))

(defrecord Router [routes])

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
  [router name]
  (get (:routes router) (keyword name)))

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
  "Find the route `name` in `router` and return the Ring request."
  ([request]
   request)
  ([router request]
   (if (map? request)
     (resolve-route router nil request)
     (resolve-route router request nil)))
  ([router name request]
   (if-let [route (find-route router name)]
     (merge (dissoc router :routes) route request
            {:uri (expand-path route request)})
     request)))

(defn- match-path [path route]
  (if-let [matches (re-matches (:path-re route) path)]
    (assoc route
           :uri path
           :path-params (zipmap (:path-params route) (rest matches)))))

(defn path-matches
  [router path & [method]]
  (let [method (or method :get)]
    (->> (vals (:routes router))
      (filter #(= method (:method %1)))
      (map (partial match-path path))
      (remove nil?))))

(defn path-for
  "Find `route-name` in `router` and return the path."
  [router route-name & [opts]]
  (if (find-route router route-name)
    (let [request (resolve-route router route-name opts)
          query (format-query-params (:query-params opts))]
      (str (:uri request) (if-not (blank? query) (str "?" query))))))

(defn request-for
  "Find `route-name` in `router` and return the request map."
  [router server route-name & [opts]]
  (if (find-route router route-name)
    (some-> (resolve-route router route-name opts)
      (merge server opts)
      (update-in [:query-params] #(into (sorted-map) %)))))

(defn url-for
  "Find `route-name` in `router` and return the url."
  [router server route-name & [opts]]
  (some-> (request-for router server route-name opts)
    (format-url)))

(defn href-for
  "Find `route-name` in `router` and return a HAL reference map."
  [router server route-name & [opts]]
  (if-let [url (url-for router server route-name opts)]
    {:href url}))

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
  `(do (def ~name
         (-> (routes.core/zip-routes ~routes)
           (routes.core/->Router)
           (merge ~opts)))
       (defn ~'path-for [~'route-name & [~'opts]]
         (routes.core/path-for ~name ~'route-name ~'opts))
       (defn ~'request-for [~'server ~'route-name & [~'opts]]
         (routes.core/request-for ~name ~'server ~'route-name ~'opts))
       (defn ~'url-for [~'server ~'route-name & [~'opts]]
         (routes.core/url-for ~name ~'server ~'route-name ~'opts))
       (defn ~'href-for [~'server ~'route-name & [~'opts]]
         (routes.core/href-for ~name ~'server ~'route-name ~'opts))))

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
