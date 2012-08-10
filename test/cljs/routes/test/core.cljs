(ns routes.test.core
  (:require-macros [routes.core :refer [defroute]])
  (:require [routes.helper :refer [route parse-url]]
            [routes.server :refer [example *server*]]
            [routes.params :as params]
            [routes.test.fixtures :as f]))

;; ROOT

(defn test-root-route []
  (let [route f/root-route]
    (assert (instance? routes.helper.Route route))
    (assert (= [] (:args route)))
    (assert (= "root" (:name route)))
    (assert (= [] (:params route)))
    (assert (= "/" (:pattern route)))
    (assert (nil? (:root route)))
    (assert (= example (:server route)))))

(defn test-root-path []
  (assert (= "/" (f/root-path))))

(defn test-root-url []
  (assert (= "https://example.com/" (f/root-url))))

;; ADDRESSES

(defn test-addresses-route []
  (let [route f/addresses-route]
    (assert (instance? routes.helper.Route route))
    (assert (= [] (:args route)))
    (assert (= "addresses" (:name route)))
    (assert (= [] (:params route)))
    (assert (= "/addresses" (:pattern route)))
    (assert (= f/root-route (:root route)))
    (assert (= example (:server route)))))

(defn test-addresses-path []
  (assert (= "/addresses" (f/addresses-path))))

(defn test-addresses-url []
  (assert (= "https://example.com/addresses" (f/addresses-url))))

;; ADDRESS

(defn test-address-route []
  (let [route f/address-route]
    (assert (instance? routes.helper.Route route))
    (assert (= '[address] (:args route)))
    (assert (= "address" (:name route)))
    (assert (= [[(assoc params/location :name "location")]] (:params route)))
    (assert (= "/addresses/%s" (:pattern route)))
    (assert (= f/addresses-route (:root route)))
    (assert (= example (:server route)))))

(defn test-address-path []
  (assert (= "/addresses/43.4073349,-2.6983217"
             (f/address-path f/address-of-mundaka))))

(defn test-address-url []
  (assert (= "https://example.com/addresses/43.4073349,-2.6983217"
             (f/address-url f/address-of-mundaka))))

;; CONTINENTS

(defn test-continents-route []
  (let [route f/continents-route]
    (assert (instance? routes.helper.Route route))
    (assert (= [] (:args route)))
    (assert (= "continents" (:name route)))
    (assert (= [] (:params route)))
    (assert (= "/continents" (:pattern route)))
    (assert (= f/root-route (:root route)))
    (assert (= example (:server route)))))

(defn test-continents-path []
  (assert (= "/continents" (f/continents-path))))

(defn test-continents-url []
  (assert (= "https://example.com/continents" (f/continents-url))))

;; CONTINENT

(defn test-continent-path []
  (assert (= "/continents/1-europe" (f/continent-path f/europe))))

(defn test-continent-url []
  (assert (= "https://example.com/continents/1-europe" (f/continent-url f/europe))) )

(defn test-continent-route []
  (let [route f/continent-route]
    (assert (instance? routes.helper.Route route))
    (assert (= ['continent] (:args route)))
    (assert (= "continent" (:name route)))
    (assert (= [[(assoc params/integer :name "id")
                 (assoc params/string :name "name")]]
               (:params route)))
    (assert (= "/continents/%s-%s" (:pattern route)))
    (assert (= f/continents-route (:root route)))
    (assert (= example (:server route)))))

;; COUNTRIES

(defn test-countries-path []
  (assert (= "/countries" (f/countries-path))))

(defn test-countries-url []
  (assert (= "https://example.com/countries" (f/countries-url))))

(defn test-countries-route []
  (let [route f/countries-route]
    (assert (instance? routes.helper.Route route))
    (assert (= [] (:args route)))
    (assert (= "countries" (:name route)))
    (assert (= [] (:params route)))
    (assert (= "/countries" (:pattern route)))
    (assert (= f/root-route (:root route)))
    (assert (= example (:server route)))))

(defn test-countries-path []
  (assert (= "/countries" (f/countries-path))))

(defn test-countries-url []
  (assert (= "https://example.com/countries" (f/countries-url))))

;; COUNTRIES OF CONTINENT

(defn test-countries-of-continent-route []
  (let [route f/countries-of-continent-route]
    (assert (instance? routes.helper.Route route))
    (assert (= '[continent] (:args route)))
    (assert (= "countries-of-continent" (:name route)))
    (assert (= [[(assoc params/integer :name "id")
                 (assoc params/string :name "name")]]
               (:params route)))
    (assert (= "/continents/%s-%s/countries" (:pattern route)))
    (assert (= f/continent-route (:root route)))
    (assert (= example (:server route)))))

(defn test-countries-of-continent-path []
  (assert (= "/continents/1-europe/countries"
             (f/countries-of-continent-path f/europe))))

(defn test-countries-of-continent-url []
  (assert (= "https://example.com/continents/1-europe/countries"
             (f/countries-of-continent-url f/europe))))

;; COUNTRY OF CONTINENT

(defn test-country-of-continent-route []
  (let [route f/country-of-continent-route]
    (assert (instance? routes.helper.Route route))
    (assert (= '[continent country] (:args route)))
    (assert (= "country-of-continent" (:name route)))
    (assert (= [[(assoc params/integer :name "id")
                 (assoc params/string :name "name")]
                [(assoc params/iso-3166-1-alpha-2 :name "iso-3166-1-alpha-2")
                 (assoc params/string :name "name")]]
               (:params route)))
    (assert (= "/continents/%s-%s/countries/%s-%s" (:pattern route)))
    (assert (= f/countries-of-continent-route (:root route)))
    (assert (= example (:server route)))))

(defn test-country-of-continent-path []
  (assert (= "/continents/1-europe/countries/es-spain"
             (f/country-of-continent-path f/europe f/spain))))

(defn test-country-of-continent-url []
  (assert (= "https://example.com/continents/1-europe/countries/es-spain"
             (f/country-of-continent-url f/europe f/spain))))

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
  (test-country-of-continent-route)
  (test-country-of-continent-path)
  (test-country-of-continent-url))