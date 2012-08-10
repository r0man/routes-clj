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

(defrecord Route [name args route-pattern route-params root])

(defn link-to
  "Wraps some content in a HTML hyperlink with the supplied URL."
  [& args]
  (let [href (if (string? (first args)) (first args))
        content (rest args)
        content (if-not (empty? content) content)]
    [:a {:href href} content]))

(defn route
  "Lookup a route by `name`."
  [name] (get @*routes* (keyword name)))

(defn register
  "Register `route` by it's name."
  [route] (swap! *routes* assoc (keyword (str (:ns route) "/" (:name route))) route))

(defn read-vector [s]
  (->> (map #(keyword (replace %1 #"^:" ""))
            (remove blank? (split (replace (str s) #"\[|\]" "") #"\s+")))
       (apply vector)))

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
  (-> (reduce
       (fn [[coll skip groups] count]
         [(drop skip coll) count (conj groups (take count coll))])
       [coll 0 []] counts)
      (last)))

(defn make-params [pattern & bindings]
  (let [keys (parse-keys pattern)]
    (assert (= (count (flatten keys)) (count bindings)))
    (->> (map
          (fn [keys params] (map #(assoc %2 :name (name %1)) keys params))
          keys (split-by bindings (map count keys)))
         (apply vector))))

(defn parse-pattern [pattern]
  (replace
   (str pattern)
   #"\:[^:]+"
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

(defn format-path [route & args]
  (->> (map (fn [arg params]
              (map #((:format-fn %1) (get arg (keyword (:name %1)))) params))
            args (:params route))
       (flatten)
       (apply format (:pattern route))))

(defn format-url [route & args]
  (str (server-url (or *server* (:server route)))
       (apply format-path route args)))

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
