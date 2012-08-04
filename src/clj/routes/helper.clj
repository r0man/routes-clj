(ns routes.helper
  (:refer-clojure :exclude [replace])
  (:require [clojure.string :refer [blank? join lower-case split replace replace-first]]
            [inflections.core :refer [parameterize]]
            [inflections.number :refer [parse-integer]]
            [routes.server :refer [*server* server-url]]))

(def ^:dynamic *routes* (atom {}))

(defrecord Route [name args pattern params])

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
  [route] (swap! *routes* assoc (keyword (:name route)) route))

(defn identifier [resource keyseq]
  (->> (map resource keyseq)
       (map str)
       (remove blank?)
       (map lower-case)
       (map parameterize)
       (join "-")))

(defn path
  "Make a path by joining `segments` with a slash."
  [& segments]
  (->> (map str segments)
       (remove blank?)
       (map #(replace %1 #"^/+" ""))
       (join "/")
       (str "/")))

(defn read-vector [s]
  (->> (map #(keyword (replace %1 #"^:" ""))
            (remove blank? (split (replace (str s) #"\[|\]" "") #"\s+")))
       (apply vector)))

(defn parse-keys [pattern]
  (->> (split pattern #"/")
       (map #(apply vector (map read-vector (re-seq #"\[[^]]+\]" %1))))
       (remove empty?)
       (apply vector)))

(defn parse-pattern [pattern]
  (replace (str pattern) #"\[[^]]+\]" "%s"))

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
  (apply format (:pattern route)
         (mapcat
          (fn [[m args]]
            (map parameterize (map #(get-in m %1) args)))
          (map vector args (:params route)))))

(defn format-url [route & args]
  (str (server-url (or *server* (:server route)))
       (apply format-path route args)))
