(ns routes.helper
  (:refer-clojure :exclude (replace))
  (:require [clojure.string :refer [blank? join lower-case split replace replace-first]]
            [inflections.core :refer [parameterize]]))

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
  [route] (swap! *routes* assoc (:name route) route))

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

(defn format-pattern [pattern & args]
  (reduce
   (fn [pattern [keyseq arg]]
     (reduce #(replace-first
               %1 (str %2)
               (parameterize (lower-case (str (get arg %2)))))
             pattern keyseq))
   pattern (map vector (parse-keys pattern) args)))
