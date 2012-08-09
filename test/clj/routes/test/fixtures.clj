(ns routes.test.fixtures
  (:require [routes.core :refer [defroute]]
            [routes.param :as params]
            [routes.server :refer [example]]))

(def europe {:id 1 :name "Europe"})

(def spain {:iso-3166-1-alpha-2 "es" :name "Spain"})

(def address-of-mundaka {:location {:latitude 43.4073349 :longitude -2.6983217}})

(defroute root []
  ["/"] :server example)

(defroute addresses []
  ["/addresses"]
  :root root-route)

(defroute address [address]
  ["/:location" params/location]
  :root addresses-route)

(defroute continents []
  ["/continents"]
  :root root-route
  :query-params {:location params/location})

(defroute continent [continent]
  ["/:id-:name" params/integer params/string]
  :root continents-route)

(defroute countries []
  ["/countries"]
  :root root-route)

(defroute country []
  ["/:iso-3166-1-alpha-2-:name" params/iso-3166-1-alpha-2 params/string]
  :root countries-route)

(defroute countries-of-continent []
  ["/countries"] :root continent-route)

(defroute country-of-continent [country]
  ["/:iso-3166-1-alpha-2-:name" params/iso-3166-1-alpha-2 params/string]
  :root countries-of-continent-route)
