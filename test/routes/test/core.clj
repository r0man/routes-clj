(ns routes.test.core
  (:require [routes.params :as params])
  (:use clojure.test
        routes.core
        routes.helper))

(def europe {:id 1 :name "Europe"})

(def spain {:iso-3166-1-alpha-2 "es" :name "Spain"})

(def address-of-mundaka {:location {:latitude 43.4073349 :longitude -2.6983217}})

(def ^:dynamic *server*
  {:scheme :https :server-name "example.com" :server-port 443})

(defroute root []
  ["/"] :server *server*)

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
  :server *server*)

;; ROOT

(deftest test-root-route
  (let [route (root-route)]
    (is (route? route))
    (is (= "routes.test.core" (:ns route)))
    (is (= "root-route" (:name route)))
    (is (= [] (route-args route)))
    (is (= [] (route-params route)))
    (is (= "/" (route-pattern route)))
    (is (nil? (:root route)))
    (is (= *server* (route-server route)))))

(deftest test-root-path
  (is (= "/" (root-path))))

(deftest test-root-url
  (is (= "https://example.com/" (root-url))))

(deftest test-root-request
  (let [request (root-request)]
    (is (= :get (:request-method request)))
    (is (= (root-path) (:uri request)))
    (is (= (:server-name *server*) (:server-name request)))))

;; ADDRESSES

(deftest test-addresses-route
  (let [route (addresses-route)]
    (is (route? route))
    (is (= "routes.test.core" (:ns route)))
    (is (= "addresses-route" (:name route)))
    (is (= [] (route-args route)))
    (is (= [] (route-params route)))
    (is (= "/addresses" (route-pattern route)))
    (is (= (root-route) (:root route)))
    (is (= *server* (route-server route)))))

(deftest test-addresses-path
  (is (= "/addresses" (addresses-path))))

(deftest test-addresses-url
  (is (= "https://example.com/addresses" (addresses-url))))

(deftest test-addresses-request
  (let [request (addresses-request)]
    (is (= :get (:request-method request)))
    (is (= (addresses-path) (:uri request)))
    (is (= (:server-name *server*) (:server-name request)))))

;; ADDRESS

(deftest test-address-route
  (let [route (address-route)]
    (is (route? route))
    (is (= "routes.test.core" (:ns route)))
    (is (= "address-route" (:name route)))
    (is (= '[address] (route-args route)))
    (is (= [[:location params/location]] (route-params route)))
    (is (= "/addresses/%s" (route-pattern route)))
    (is (= (addresses-route) (:root route)))
    (is (= *server* (route-server route)))))

(deftest test-address-path
  (is (= "/addresses/43.4073349,-2.6983217"
         (address-path address-of-mundaka))))

(deftest test-address-url
  (is (= "https://example.com/addresses/43.4073349,-2.6983217"
         (address-url address-of-mundaka))))

(deftest test-address-request
  (let [request (address-request address-of-mundaka)]
    (is (= :get (:request-method request)))
    (is (= (address-path address-of-mundaka) (:uri request)))
    (is (= (:server-name *server*) (:server-name request)))))

;; CONTINENTS

(deftest test-continents-route
  (let [route (continents-route)]
    (is (route? route))
    (is (= "routes.test.core" (:ns route)))
    (is (= "continents-route" (:name route)))
    (is (= [] (route-args route)))
    (is (= [] (route-params route)))
    (is (= "/continents" (route-pattern route)))
    (is (= (root-route) (:root route)))
    (is (= *server* (route-server route)))))

(deftest test-continents-path
  (is (= "/continents" (continents-path)))
  (is (= "/continents?page=1" (continents-path {:page 1}))))

(deftest test-continents-url
  (is (= "https://example.com/continents" (continents-url)))
  (is (= "https://example.com/continents?page=1" (continents-url {:page 1}))))

(deftest test-continents-request
  (let [request (continents-request)]
    (is (= :get (:request-method request)))
    (is (= (continents-path) (:uri request)))
    (is (= (:server-name *server*) (:server-name request))))
  (let [request (continents-request {:page 1})]
    (is (= :get (:request-method request)))
    (is (= (continents-path) (:uri request)))
    (is (= (:server-name *server*) (:server-name request)))
    (is (= {:page 1} (:query-params request)))))

;; CONTINENT

(deftest test-continent-route
  (let [route (continent-route)]
    (is (route? route))
    (is (= "routes.test.core" (:ns route)))
    (is (= "continent-route" (:name route)))
    (is (= ['continent] (route-args route)))
    (is (= [[:id params/integer :name params/string]]
           (route-params route)))
    (is (= "/continents/%s-%s" (route-pattern route)))
    (is (= (continents-route) (:root route)))
    (is (= *server* (route-server route)))))

(deftest test-continent-path
  (is (= "/continents/1-europe" (continent-path europe))))

(deftest test-continent-url
  (is (= "https://example.com/continents/1-europe" (continent-url europe))) )

(deftest test-continent-request
  (let [request (continent-request europe)]
    (is (= :get (:request-method request)))
    (is (= (continent-path europe) (:uri request)))
    (is (= (:server-name *server*) (:server-name request)))))

;; COUNTRIES

(deftest test-countries-route
  (let [route (countries-route)]
    (is (route? route))
    (is (= "routes.test.core" (:ns route)))
    (is (= "countries-route" (:name route)))
    (is (= [] (route-args route)))
    (is (= [] (route-params route)))
    (is (= "/countries" (route-pattern route)))
    (is (= (root-route) (:root route)))
    (is (= *server* (route-server route)))))

(deftest test-countries-path
  (is (= "/countries" (countries-path))))

(deftest test-countries-url
  (is (= "https://example.com/countries" (countries-url))))

(deftest test-countries-request
  (let [request (countries-request)]
    (is (= :get (:request-method request)))
    (is (= (countries-path) (:uri request)))
    (is (= (:server-name *server*) (:server-name request)))))

;; COUNTRIES OF CONTINENT

(deftest test-countries-of-continent-route
  (let [route (countries-of-continent-route)]
    (is (route? route))
    (is (= "routes.test.core" (:ns route)))
    (is (= "countries-of-continent-route" (:name route)))
    (is (= '[continent] (route-args route)))
    (is (= [[:id params/integer :name params/string]]
           (route-params route)))
    (is (= "/continents/%s-%s/countries" (route-pattern route)))
    (is (= (continent-route) (:root route)))
    (is (= *server* (route-server route)))))

(deftest test-countries-of-continent-path
  (is (= "/continents/1-europe/countries"
         (countries-of-continent-path europe)))
  (is (= "/continents/1-europe/countries?page=1"
         (countries-of-continent-path europe {:page 1}))))

(deftest test-countries-of-continent-url
  (is (= "https://example.com/continents/1-europe/countries"
         (countries-of-continent-url europe)))
  (is (= "https://example.com/continents/1-europe/countries?page=1"
         (countries-of-continent-url europe {:page 1}))))

(deftest test-countries-of-continent-request
  (let [request (countries-of-continent-request europe {:page 1})]
    (is (= :get (:request-method request)))
    (is (= (countries-of-continent-path europe) (:uri request)))
    (is (= (:server-name *server*) (:server-name request)))
    (is (= {:page 1} (:query-params request)))))

;; COUNTRY OF CONTINENT #1

(deftest test-country-of-continent-1-route
  (let [route (country-of-continent-1-route)]
    (is (route? route))
    (is (= "routes.test.core" (:ns route)))
    (is (= "country-of-continent-1-route" (:name route)))
    (is (= '[continent country] (route-args route)))
    (is (= [[:id params/integer :name params/string]
            [:iso-3166-1-alpha-2 params/iso-3166-1-alpha-2 :name params/string]]
           (route-params route)))
    (is (= "/continents/%s-%s/countries/%s-%s" (route-pattern route)))
    (is (= (countries-of-continent-route) (:root route)))
    (is (= *server* (route-server route)))))

(deftest test-country-of-continent-1-path
  (is (= "/continents/1-europe/countries/es-spain"
         (country-of-continent-1-path europe spain))))

(deftest test-country-of-continent-1-url
  (is (= "https://example.com/continents/1-europe/countries/es-spain"
         (country-of-continent-1-url europe spain))))

(deftest test-country-of-continent-1-request
  (let [request (country-of-continent-1-request europe spain)]
    (is (= :get (:request-method request)))
    (is (= (country-of-continent-1-path europe spain) (:uri request)))
    (is (= (:server-name *server*) (:server-name request)))))

;; COUNTRY OF CONTINENT #2

(deftest test-country-of-continent-2-route
  (let [route (country-of-continent-2-route)]
    (is (route? route))
    (is (= "routes.test.core" (:ns route)))
    (is (= "country-of-continent-2-route" (:name route)))
    (is (= '[continent country] (route-args route)))
    (is (= [[:id params/integer :name params/string]
            [:iso-3166-1-alpha-2 params/iso-3166-1-alpha-2 :name params/string]]
           (route-params route)))
    (is (= "/continents/%s-%s/countries/%s-%s" (route-pattern route)))
    (is (nil? (:root route)))
    (is (= *server* (route-server route)))))

(deftest test-country-of-continent-2-path
  (is (= "/continents/1-europe/countries/es-spain"
         (country-of-continent-2-path europe spain))))

(deftest test-country-of-continent-2-url
  (is (= "https://example.com/continents/1-europe/countries/es-spain"
         (country-of-continent-2-url europe spain))))

(deftest test-country-of-continent-2-request
  (let [request (country-of-continent-2-request europe spain)]
    (is (= :get (:request-method request)))
    (is (= (country-of-continent-2-path europe spain) (:uri request)))
    (is (= (:server-name *server*) (:server-name request)))))

(deftest test-url-with-binding
  (binding [*server* {:scheme :https :server-name "other.com" :server-port 443}]
    (is (= "https://other.com/" (root-url)))))
