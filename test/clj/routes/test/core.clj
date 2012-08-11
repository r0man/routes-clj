(ns routes.test.core
  (:require [routes.params :as params])
  (:use clojure.test
        routes.core
        routes.helper
        routes.server))

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

(deftest test-root-route
  (let [route root-route]
    (is (route? route))
    (is (= [] (route-args route)))
    (is (= 'root-route (:name route)))
    (is (= [] (route-params route)))
    (is (= "/" (route-pattern route)))
    (is (nil? (:root route)))
    (is (= example (route-server route)))))

(deftest test-root-path
  (is (= "/" (root-path))))

(deftest test-root-url
  (is (= "https://example.com/" (root-url))))

;; ADDRESSES

(deftest test-addresses-route
  (let [route addresses-route]
    (is (route? route))
    (is (= 'routes.test.core (:ns route)))
    (is (= 'addresses-route (:name route)))
    (is (= [] (route-args route)))
    (is (= [] (route-params route)))
    (is (= "/addresses" (route-pattern route)))
    (is (= root-route (:root route)))
    (is (= example (route-server route)))))

(deftest test-addresses-path
  (is (= "/addresses" (addresses-path))))

(deftest test-addresses-url
  (is (= "https://example.com/addresses" (addresses-url))))

;; ADDRESS

(deftest test-address-route
  (let [route address-route]
    (is (route? route))
    (is (= 'routes.test.core (:ns route)))
    (is (= 'address-route (:name route)))
    (is (= '[address] (route-args route)))
    (is (= [[:location params/location]] (route-params route)))
    (is (= "/addresses/%s" (route-pattern route)))
    (is (= addresses-route (:root route)))
    (is (= example (route-server route)))))

(deftest test-address-path
  (is (= "/addresses/43.4073349,-2.6983217"
         (address-path address-of-mundaka))))

(deftest test-address-url
  (is (= "https://example.com/addresses/43.4073349,-2.6983217"
         (address-url address-of-mundaka))))

;; CONTINENTS

(deftest test-continents-route
  (let [route continents-route]
    (is (route? route))
    (is (= 'routes.test.core (:ns route)))
    (is (= 'continents-route (:name route)))
    (is (= [] (route-args route)))
    (is (= [] (route-params route)))
    (is (= "/continents" (route-pattern route)))
    (is (= root-route (:root route)))
    (is (= example (route-server route)))))

(deftest test-continents-path
  (is (= "/continents" (continents-path))))

(deftest test-continents-url
  (is (= "https://example.com/continents" (continents-url))))

;; CONTINENT

(deftest test-continent-route
  (let [route continent-route]
    (is (route? route))
    (is (= 'routes.test.core (:ns route)))
    (is (= 'continent-route (:name route)))
    (is (= ['continent] (route-args route)))
    (is (= [[:id params/integer :name params/string]]
           (route-params route)))
    (is (= "/continents/%s-%s" (route-pattern route)))
    (is (= continents-route (:root route)))
    (is (= example (route-server route)))))

(deftest test-continent-path
  (is (= "/continents/1-europe" (continent-path europe))))

(deftest test-continent-url
  (is (= "https://example.com/continents/1-europe" (continent-url europe))) )

;; COUNTRIES

(deftest test-countries-route
  (let [route countries-route]
    (is (route? route))
    (is (= 'routes.test.core (:ns route)))
    (is (= 'countries-route (:name route)))
    (is (= [] (route-args route)))
    (is (= [] (route-params route)))
    (is (= "/countries" (route-pattern route)))
    (is (= root-route (:root route)))
    (is (= example (route-server route)))))

(deftest test-countries-path
  (is (= "/countries" (countries-path))))

(deftest test-countries-url
  (is (= "https://example.com/countries" (countries-url))))

;; COUNTRIES OF CONTINENT

(deftest test-countries-of-continent-route
  (let [route countries-of-continent-route]
    (is (route? route))
    (is (= 'routes.test.core (:ns route)))
    (is (= 'countries-of-continent-route (:name route)))
    (is (= '[continent] (route-args route)))
    (is (= [[:id params/integer :name params/string]]
           (route-params route)))
    (is (= "/continents/%s-%s/countries" (route-pattern route)))
    (is (= continent-route (:root route)))
    (is (= example (route-server route)))))

(deftest test-countries-of-continent-path
  (is (= "/continents/1-europe/countries"
         (countries-of-continent-path europe))))

(deftest test-countries-of-continent-url
  (is (= "https://example.com/continents/1-europe/countries"
         (countries-of-continent-url europe))))

;; COUNTRY OF CONTINENT #1

(deftest test-country-of-continent-1-route
  (let [route country-of-continent-1-route]
    (is (route? route))
    (is (= 'routes.test.core (:ns route)))
    (is (= 'country-of-continent-1-route (:name route)))
    (is (= '[continent country] (route-args route)))
    (is (= [[:id params/integer :name params/string]
            [:iso-3166-1-alpha-2 params/iso-3166-1-alpha-2 :name params/string]]
           (route-params route)))
    (is (= "/continents/%s-%s/countries/%s-%s" (route-pattern route)))
    (is (= countries-of-continent-route (:root route)))
    (is (= example (route-server route)))))

(deftest test-country-of-continent-1-path
  (is (= "/continents/1-europe/countries/es-spain"
         (country-of-continent-1-path europe spain))))

(deftest test-country-of-continent-1-url
  (is (= "https://example.com/continents/1-europe/countries/es-spain"
         (country-of-continent-1-url europe spain))))

;; COUNTRY OF CONTINENT #2

(deftest test-country-of-continent-2-route
  (let [route country-of-continent-2-route]
    (is (route? route))
    (is (= 'routes.test.core (:ns route)))
    (is (= 'country-of-continent-2-route (:name route)))
    (is (= '[continent country] (route-args route)))
    ;; (is (= [[:id params/integer :name params/string]
    ;;         [:iso-3166-2-alpha-2 params/iso-3166-1-alpha-2 :name params/string]]
    ;;        (route-params route)))
    (is (= "/continents/%s-%s/countries/%s-%s" (route-pattern route)))
    (is (nil? (:root route)))
    (is (= example (route-server route)))))

(deftest test-country-of-continent-2-path
  (is (= "/continents/1-europe/countries/es-spain"
         (country-of-continent-2-path europe spain))))

(deftest test-country-of-continent-2-url
  (is (= "https://example.com/continents/1-europe/countries/es-spain"
         (country-of-continent-2-url europe spain))))
