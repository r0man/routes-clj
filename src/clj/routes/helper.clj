(ns routes.helper
  (:refer-clojure :exclude [replace])
  (:require [clojure.string :refer [blank? join split replace]]
            [inflections.core :refer [parameterize]]
            [inflections.util :refer [parse-integer]]
            [routes.server :refer [server-url]]))

(def ^:dynamic *routes* (atom {}))

(defrecord Route [ns name root args pattern params server])

(defn link-to
  "Wraps some content in a HTML hyperlink with the supplied URL."
  [& args]
  (let [href (if (string? (first args)) (first args))
        content (rest args)
        content (if-not (empty? content) content)]
    [:a {:href href} content]))

(defn format-params [params]
  (->> (seq params)
       (map (fn [[k v]] (str (name k) "=" (str v))))
       (join "&")))

(defn route
  "Lookup a route by `symbol`."
  [sym] (get @*routes* sym))

(defn route-symbol
  "Returns the namespace qualified name of `route` as a symbol."
  [route] (if route (symbol (str (:ns route) "/" (:name route)))))

(defn register
  "Register `route` by it's name."
  [route]
  (swap! *routes* assoc (route-symbol route) route)
  route)

(defn parse-keys [pattern]
  (->> (split pattern #"/")
       (map (fn [segment]
              (->> (re-seq #"\:[^:]+" segment)
                   (map #(replace %1 ":" ""))
                   (map #(replace %1 #"-$" ""))
                   (map keyword)
                   (apply vector))))
       (remove empty?)
       (apply vector)))

(defn split-by [coll counts]
  (last (reduce
         (fn [[coll skip groups] count]
           [(drop count coll) count (conj groups (take count coll))])
         [coll 0 []] counts)))

(defn make-params [pattern & bindings]
  (let [keys (parse-keys pattern)]
    (assert (= (count (flatten keys)) (count bindings)))
    (->> (map
          (fn [keys params]
            (apply vector (interleave keys params)))
          keys (split-by bindings (map count keys)))
         (apply vector))))

(defn parse-pattern [pattern]
  (replace
   (str pattern)
   #"\:[^:/]+"
   #(str "%s" (if (= \- (last %1)) "-"))))

(defn parse-url [url]
  (cond
   (map? url) url
   (string? url)
   (if-let [matches (re-find #"(([^:]+)://)?([^:/]+)(:(\d+))?(/.*)?" url)]
     (let [scheme (keyword (or (nth matches 2) :https))
           port (parse-integer (nth matches 5))]
       {:scheme scheme
        :server-name (nth matches 3)
        :server-port (cond
                      (integer? port) port
                      (= :http scheme) 80
                      (= :https scheme) 443)
        :uri (or (nth matches 6) "/")}))))

(defn path
  "Make a path by joining `segments` with a slash."
  [& segments]
  (->> (map str segments)
       (remove blank?)
       (map #(replace %1 #"^/+" ""))
       (map #(replace %1 #"/+$" ""))
       (remove blank?)
       (join "/")
       (str "/")))

(defn route?
  "Returns true if `arg` is a route, otherwise false."
  [arg] (instance? Route arg))

(defn route-root
  "Returns the root of the `route`."
  [route] (if (fn? (:root route))
            ((:root route))
            (:root route)))

(defn route-args
  "Returns the arguments of the `route`."
  [route] (if route (concat (route-args (route-root route)) (:args route))))

(defn route-pattern
  "Returns the pattern of the `route`."
  [route] (if route (path (route-pattern (route-root route)) (:pattern route))))

(defn route-params
  "Returns the arguments of the `route`."
  [route] (if route (concat (route-params (route-root route)) (:params route))))

(defn route-path
  "Format the path of `route`."
  [route args & [params]]
  (str (->> (map
             (fn [[arg params]]
               (map (fn [[attr param]]
                      (if-let [v (get arg attr)]
                        ((:format-fn param) v)))
                    (partition 2 params)))
             (partition 2 (interleave args (route-params route))))
            (flatten)
            (apply format (route-pattern route)))
       (if-not (empty? params)
         (str "?" (format-params params)))))

(defn route-server
  "Returns the server of the `route`."
  [route] (if route (or (:server route) (route-server (route-root route)))))

(defn route-url
  "Format the url of `route`."
  [route args & [params]]
  (str (server-url (:server route))
       (route-path route args params)))

(defn make-route
  "Make a new route."
  [ns name args pattern params & [options]]
  (map->Route
   {:ns (str ns)
    :name (str name)
    :root (route-root options)
    :args args
    :pattern (parse-pattern pattern)
    :params (apply make-params pattern params)
    :server (route-server options)}))

(defn qualified?
  "Returns true if the `s` is namespace qualified, otherwise false."
  [s] (if s (re-matches #".+/.+" (str s))))
