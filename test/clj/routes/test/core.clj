(ns routes.test.core
  (:require [routes.param :as params])
  (:use clojure.test
        routes.core
        routes.helper
        routes.server
        routes.test.fixtures)
  (:import routes.helper.Route))

;; ROOT

(deftest test-root-route
  (let [route root-route]
    (is (instance? routes.helper.Route route))
    (is (= [] (:args route)))
    (is (= "root" (:name route)))
    (is (= [] (:params route)))
    (is (= "/" (:pattern route)))
    (is (nil? (:root route)))
    (is (= example (:server route)))))

(deftest test-root-path
  (is (= "/" (root-path))))

(deftest test-root-url
  (is (= "https://example.com/" (root-url))))

;; ADDRESSES

(deftest test-addresses-route
  (let [route addresses-route]
    (is (instance? routes.helper.Route route))
    (is (= [] (:args route)))
    (is (= "addresses" (:name route)))
    (is (= [] (:params route)))
    (is (= "/addresses" (:pattern route)))
    (is (= root-route (:root route)))
    (is (= example (:server route)))))

(deftest test-addresses-path
  (is (= "/addresses" (addresses-path))))

(deftest test-addresses-url
  (is (= "https://example.com/addresses" (addresses-url))))

;; ADDRESS

(deftest test-address-route
  (let [route address-route]
    (is (instance? routes.helper.Route route))
    (is (= '[address] (:args route)))
    (is (= "address" (:name route)))
    (is (= [[(assoc params/location :name "location")]] (:params route)))
    (is (= "/addresses/%s" (:pattern route)))
    (is (= addresses-route (:root route)))
    (is (= example (:server route)))))

(deftest test-address-path
  (is (= "/addresses/43.4073349,-2.6983217"
         (address-path address-of-mundaka))))

(deftest test-address-url
  (is (= "https://example.com/addresses/43.4073349,-2.6983217"
         (address-url address-of-mundaka))))

;; ;; CONTINENTS

(deftest test-continents-route
  (let [route continents-route]
    (is (instance? routes.helper.Route route))
    (is (= [] (:args route)))
    (is (= "continents" (:name route)))
    (is (= [] (:params route)))
    (is (= "/continents" (:pattern route)))
    (is (= root-route (:root route)))
    (is (= example (:server route)))))

(deftest test-continents-path
  (is (= "/continents" (continents-path))))

(deftest test-continents-url
  (is (= "https://example.com/continents" (continents-url))))

;; CONTINENT

(deftest test-continent-path
  (is (= "/continents/1-europe" (continent-path europe))))

(deftest test-continent-url
  (is (= "https://example.com/continents/1-europe" (continent-url europe))) )

(deftest test-continent-route
  (let [route continent-route]
    (is (instance? routes.helper.Route route))
    (is (= ['continent] (:args route)))
    (is (= "continent" (:name route)))
    (is (= [[(assoc params/integer :name "id")
             (assoc params/string :name "name")]]
           (:params route)))
    (is (= "/continents/%s-%s" (:pattern route)))
    (is (= continents-route (:root route)))
    (is (= example (:server route)))))

;; COUNTRIES

(deftest test-countries-path
  (is (= "/countries" (countries-path))))

(deftest test-countries-url
  (is (= "https://example.com/countries" (countries-url))))

(deftest test-countries-route
  (let [route countries-route]
    (is (instance? routes.helper.Route route))
    (is (= [] (:args route)))
    (is (= "countries" (:name route)))
    (is (= [] (:params route)))
    (is (= "/countries" (:pattern route)))
    (is (= root-route (:root route)))
    (is (= example (:server route)))))

(deftest test-countries-path
  (is (= "/countries" (countries-path))))

(deftest test-countries-url
  (is (= "https://example.com/countries" (countries-url))))

;; COUNTRIES OF CONTINENT

(deftest test-countries-of-continent-route
  (let [route countries-of-continent-route]
    (is (instance? routes.helper.Route route))
    (is (= '[continent] (:args route)))
    (is (= "countries-of-continent" (:name route)))
    (is (= [[(assoc params/integer :name "id")
             (assoc params/string :name "name")]]
           (:params route)))
    (is (= "/continents/%s-%s/countries" (:pattern route)))
    (is (= continent-route (:root route)))
    (is (= example (:server route)))))

;; COUNTRY OF CONTINENT

(deftest test-country-of-continent-route
  (let [route country-of-continent-route]
    (is (instance? routes.helper.Route route))
    (is (= '[continent country] (:args route)))
    (is (= "country-of-continent" (:name route)))
    (is (= [[(assoc params/integer :name "id")
             (assoc params/string :name "name")]
            [(assoc params/iso-3166-1-alpha-2 :name "iso-3166-1-alpha-2")
             (assoc params/string :name "name")]]
           (:params route)))
    (is (= "/continents/%s-%s/countries/%s-%s" (:pattern route)))
    (is (= countries-of-continent-route (:root route)))
    (is (= example (:server route)))))

(deftest test-country-of-continent-path
  (is (= "/continents/1-europe/countries/es-spain"
         (country-of-continent-path europe spain))))

(deftest test-country-of-continent-url
  (is (= "https://example.com/continents/1-europe/countries/es-spain"
         (country-of-continent-url europe spain))))
