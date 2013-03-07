(ns routes.params
  (:refer-clojure :exclude [format])
  (:require;*CLJSBUILD-REMOVE*;-macros
   [routes.core :refer [defparam]])
  (:require [clojure.string :refer [blank? lower-case join]]
            [inflections.core :refer [parameterize]]
            [inflections.util :refer [parse-float parse-integer parse-location]]))

(defprotocol IParameter
  (-format [param obj] "Format `obj` as a route `param`.")
  (-parse [param s] "Parse `s` into a route `param`."))

(defrecord Parameter [name doc format-fn parse-fn]
  IParameter
  (-format [param obj]
    (if-not (nil? obj) (format-fn obj)))
  (-parse [param s]
    (if-not (nil? s) (parse-fn s))))

(defn parse-iso-3166-1-alpha-2 [s]
  (lower-case (apply str (take 2 (str s)))))

(defn parse-iso-3166-1-alpha-3 [s]
  (lower-case (apply str (take 3 (str s)))))

(defn parse-iso-639-1 [s]
  (lower-case (apply str (take 2 (str s)))))

(defparam iso-3166-1-alpha-2
  "A two-letter country code defined in the ISO 3166-1 standard."
  parse-iso-3166-1-alpha-2
  parse-iso-3166-1-alpha-2)

(defparam iso-3166-1-alpha-3
  "A three-letter country code defined in the ISO 3166-1 standard."
  parse-iso-3166-1-alpha-3
  parse-iso-3166-1-alpha-3)

(defparam iso-639-1
  "A two-letter language code defined in the ISO 639-1 standard."
  parse-iso-639-1
  parse-iso-639-1)

(defparam location
  "A geographical location."
  #(str (:latitude %1) "," (:longitude %1))
  parse-location)

(defparam integer
  "An integer."
  str
  parse-integer)

(defparam string
  "A string."
  parameterize)
