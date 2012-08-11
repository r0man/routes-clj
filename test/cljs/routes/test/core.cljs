(ns routes.test.core
  (:require-macros [routes.core :refer [defroute]])
  (:require [routes.helper :refer [route route? route-args route-server route-pattern]]
            [routes.helper :refer [route-params parse-url]]
            [routes.server :refer [example]]
            [routes.params :as params]))

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
  :root root-route)

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

(defroute country-of-continent-1 [country]
  ["/:iso-3166-1-alpha-2-:name" params/iso-3166-1-alpha-2 params/string]
  :root countries-of-continent-route)

(defroute country-of-continent-2 [continent country]
  ["/continents/:id-:name/countries/:iso-3166-1-alpha-2-:name"
   params/integer params/string
   params/iso-3166-1-alpha-2 params/string]
  :server example)

;; ROOT

(defn test-root-route []
  (let [route root-route]
    (assert (route? route))
    (assert (= [] (route-args route)))
    (assert (= 'root-route (:name route)))
    (assert (= [] (route-params route)))
    (assert (= "/" (route-pattern route)))
    (assert (nil? (:root route)))
    (assert (= example (route-server route)))))

(defn test-root-path []
  (assert (= "/" (root-path))))

(defn test-root-url []
  (assert (= "https://example.com/" (root-url))))

;; ADDRESSES

(defn test-addresses-route []
  (let [route addresses-route]
    (assert (route? route))
    (assert (= 'routes.test.core (:ns route)))
    (assert (= 'addresses-route (:name route)))
    (assert (= [] (route-args route)))
    (assert (= [] (route-params route)))
    (assert (= "/addresses" (route-pattern route)))
    (assert (= root-route (:root route)))
    (assert (= example (route-server route)))))

(defn test-addresses-path []
  (assert (= "/addresses" (addresses-path))))

(defn test-addresses-url []
  (assert (= "https://example.com/addresses" (addresses-url))))

;; ADDRESS

(defn test-address-route []
  (let [route address-route]
    (assert (route? route))
    (assert (= 'routes.test.core (:ns route)))
    (assert (= 'address-route (:name route)))
    (assert (= '[address] (route-args route)))
    (assert (= [[:location params/location]] (route-params route)))
    (assert (= "/addresses/%s" (route-pattern route)))
    (assert (= addresses-route (:root route)))
    (assert (= example (route-server route)))))

(defn test-address-path []
  (assert (= "/addresses/43.4073349,-2.6983217"
             (address-path address-of-mundaka))))

(defn test-address-url []
  (assert (= "https://example.com/addresses/43.4073349,-2.6983217"
             (address-url address-of-mundaka))))

;; CONTINENTS

(defn test-continents-route []
  (let [route continents-route]
    (assert (route? route))
    (assert (= 'routes.test.core (:ns route)))
    (assert (= 'continents-route (:name route)))
    (assert (= [] (route-args route)))
    (assert (= [] (route-params route)))
    (assert (= "/continents" (route-pattern route)))
    (assert (= root-route (:root route)))
    (assert (= example (route-server route)))))

(defn test-continents-path []
  (assert (= "/continents" (continents-path))))

(defn test-continents-url []
  (assert (= "https://example.com/continents" (continents-url))))

;; CONTINENT

(defn test-continent-route []
  (let [route continent-route]
    (assert (route? route))
    (assert (= 'routes.test.core (:ns route)))
    (assert (= 'continent-route (:name route)))
    (assert (= ['continent] (route-args route)))
    (assert (= [[:id params/integer :name params/string]]
               (route-params route)))
    (assert (= "/continents/%s-%s" (route-pattern route)))
    (assert (= continents-route (:root route)))
    (assert (= example (route-server route)))))

(defn test-continent-path []
  (assert (= "/continents/1-europe" (continent-path europe))))

(defn test-continent-url []
  (assert (= "https://example.com/continents/1-europe" (continent-url europe))) )

;; COUNTRIES

(defn test-countries-route []
  (let [route countries-route]
    (assert (route? route))
    (assert (= 'routes.test.core (:ns route)))
    (assert (= 'countries-route (:name route)))
    (assert (= [] (route-args route)))
    (assert (= [] (route-params route)))
    (assert (= "/countries" (route-pattern route)))
    (assert (= root-route (:root route)))
    (assert (= example (route-server route)))))

(defn test-countries-path []
  (assert (= "/countries" (countries-path))))

(defn test-countries-url []
  (assert (= "https://example.com/countries" (countries-url))))

;; COUNTRIES OF CONTINENT

(defn test-countries-of-continent-route []
  (let [route countries-of-continent-route]
    (assert (route? route))
    (assert (= 'routes.test.core (:ns route)))
    (assert (= 'countries-of-continent-route (:name route)))
    (assert (= '[continent] (route-args route)))
    (assert (= [[:id params/integer :name params/string]]
               (route-params route)))
    (assert (= "/continents/%s-%s/countries" (route-pattern route)))
    (assert (= continent-route (:root route)))
    (assert (= example (route-server route)))))

(defn test-countries-of-continent-path []
  (assert (= "/continents/1-europe/countries"
             (countries-of-continent-path europe))))

(defn test-countries-of-continent-url []
  (assert (= "https://example.com/continents/1-europe/countries"
             (countries-of-continent-url europe))))

;; COUNTRY OF CONTINENT #1

(defn test-country-of-continent-1-route []
  (let [route country-of-continent-1-route]
    (assert (route? route))
    (assert (= 'routes.test.core (:ns route)))
    (assert (= 'country-of-continent-1-route (:name route)))
    (assert (= '[continent country] (route-args route)))
    (assert (= [[:id params/integer :name params/string]
                [:iso-3166-1-alpha-2 params/iso-3166-1-alpha-2 :name params/string]]
               (route-params route)))
    (assert (= "/continents/%s-%s/countries/%s-%s" (route-pattern route)))
    (assert (= countries-of-continent-route (:root route)))
    (assert (= example (route-server route)))))

(defn test-country-of-continent-1-path []
  (assert (= "/continents/1-europe/countries/es-spain"
             (country-of-continent-1-path europe spain))))

(defn test-country-of-continent-1-url []
  (assert (= "https://example.com/continents/1-europe/countries/es-spain"
             (country-of-continent-1-url europe spain))))

;; COUNTRY OF CONTINENT #2

(defn test-country-of-continent-2-route []
  (let [route country-of-continent-2-route]
    (assert (route? route))
    (assert (= 'routes.test.core (:ns route)))
    (assert (= 'country-of-continent-2-route (:name route)))
    (assert (= '[continent country] (route-args route)))
    (assert (= "/continents/%s-%s/countries/%s-%s" (route-pattern route)))
    (assert (nil? (:root route)))
    (assert (= example (route-server route)))))

(defn test-country-of-continent-2-path []
  (assert (= "/continents/1-europe/countries/es-spain"
             (country-of-continent-2-path europe spain))))

(defn test-country-of-continent-2-url []
  (assert (= "https://example.com/continents/1-europe/countries/es-spain"
             (country-of-continent-2-url europe spain))))

(defn test []
  (test-root-route)
  (test-root-path)
  (test-root-url)
  (test-addresses-route)
  (test-addresses-path)
  (test-addresses-url)
  (test-address-route)
  (test-address-path)
  (test-address-url)
  (test-continents-route)
  (test-continents-path)
  (test-continents-url)
  (test-continent-route)
  (test-continent-path)
  (test-continent-url)
  (test-countries-route)
  (test-countries-path)
  (test-countries-url)
  (test-countries-of-continent-route)
  (test-countries-of-continent-path)
  (test-countries-of-continent-url)
  (test-country-of-continent-1-route)
  (test-country-of-continent-1-path)
  (test-country-of-continent-1-url)
  (test-country-of-continent-2-route)
  (test-country-of-continent-2-path)
  (test-country-of-continent-2-url))