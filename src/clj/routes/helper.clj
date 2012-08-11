(ns routes.helper
  (:refer-clojure :exclude [replace])
  (:require [clojure.string :refer [blank? join split replace]]
            [inflections.core :refer [parameterize]]
            [inflections.number :refer [parse-integer]]
            [routes.server :refer [*server* server-url]]))

(def ^:dynamic *routes* (atom {}))

(defprotocol IRoute
  (-format [route args] "Format the `route`.")
  (-parse [route s] "Parse the `route`."))

(defrecord Route [])

(defn link-to
  "Wraps some content in a HTML hyperlink with the supplied URL."
  [& args]
  (let [href (if (string? (first args)) (first args))
        content (rest args)
        content (if-not (empty? content) content)]
    [:a {:href href} content]))

(defn qualified?
  "Returns true if the `s` is namespace qualified, otherwise false."
  [s] (if s (re-matches #".+/.+" (str s))))

(defn qualify [sym]
  (when sym
    (if (qualified? sym)
      (symbol sym)
      (symbol (str *ns* "/" sym)))))

(defn route
  "Lookup a route by `symbol`."
  [sym] (get @*routes* (qualify sym)))

(defn register
  "Register `route` by it's name."
  [route]
  (swap! *routes* assoc (:qualified route) route)
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

(defn route-args
  "Returns the arguments of the route."
  [route] (if route (concat (route-args (:root route)) (:args route))))

(defn route-pattern
  "Returns the pattern of the route."
  [route] (if route (path (route-pattern (:root route)) (:pattern route))))

(defn route-params
  "Returns the arguments of the route."
  [route] (if route (concat (route-params (:root route)) (:params route))))

(defn route-server
  "Returns the server of the route."
  [route] (if route (or (:server route) (route-server (:root route)))))

(defn make-route [name args [pattern & params] & [options]]
  (let [ns (symbol (str (or (:ns options) *ns*)))
        root (route (:root options))]
    (map->Route
     {:ns ns
      :name (symbol name)
      :qualified (symbol (str ns "/" name))
      :root root
      :args args
      :pattern (parse-pattern pattern)
      :params (apply make-params pattern params)
      :server (or (:server options) (route-server (:root options)))})))

(defn format-path [route & args]
  (->> (map (fn [arg params]
              (map #((:format-fn %1) (get arg (keyword (:name %1)))) params))
            args (:params route))
       (flatten)
       (apply format (:pattern route))))

(defn format-path [route & args]
  (->> (map
        (fn [[arg params]]
          (map (fn [[attr param]]
                 (if-let [v (get arg attr)]
                   ((:format-fn param) v)))
               (partition 2 params)))
        (partition 2 (interleave args (route-params route))))
       (flatten)
       (apply format (route-pattern route))))

;; (defn format-path [route & args]
;;   (interleave (route-params route) args))

(defn format-url [route & args]
  (str (server-url (or *server* (:server route)))
       (apply format-path route args)))

;; (require '[routes.params :as params])

;; (make-route 'root '[] ["/"] :server {:server-name "example.com"})

;; (make-route 'country '[]
;;   ["/:iso-3166-1-alpha-2-:name" params/iso-3166-1-alpha-2 params/string]
;;   :root 'countries-route)
