(ns routes.test.core
  (:require-macros [routes.core :refer [defroute]])
  (:require [routes.helper :refer [route route? route-args route-server route-pattern]]
            [routes.helper :refer [route-params parse-url]]
            [routes.params :as params]))

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

(defn test-root-route []
  (let [route root-route]
    (assert (route? route))
    (assert (= "routes.test.core" (:ns route)))
    (assert (= "root-route" (:name route)))
    (assert (= [] (route-args route)))
    (assert (= [] (route-params route)))
    (assert (= "/" (route-pattern route)))
    (assert (nil? (:root route)))
    (assert (= *server* (route-server route)))))

(defn test-root-path []
  (assert (= "/" (root-path))))

(defn test-root-url []
  (assert (= "https://example.com/" (root-url))))

(defn test-root-request []
  (let [request (root-request)]
    (assert (= :get (:request-method request)))
    (assert (= (root-path) (:uri request)))
    (assert (= (:server-name *server*) (:server-name request)))))

;; ADDRESSES

(defn test-addresses-route []
  (let [route addresses-route]
    (assert (route? route))
    (assert (= "routes.test.core" (:ns route)))
    (assert (= "addresses-route" (:name route)))
    (assert (= [] (route-args route)))
    (assert (= [] (route-params route)))
    (assert (= "/addresses" (route-pattern route)))
    (assert (= root-route (:root route)))
    (assert (= *server* (route-server route)))))

(defn test-addresses-path []
  (assert (= "/addresses" (addresses-path))))

(defn test-addresses-url []
  (assert (= "https://example.com/addresses" (addresses-url))))

(defn test-addresses-request []
  (let [request (addresses-request)]
    (assert (= :get (:request-method request)))
    (assert (= (addresses-path) (:uri request)))
    (assert (= (:server-name *server*) (:server-name request)))))

;; ADDRESS

(defn test-address-route []
  (let [route address-route]
    (assert (route? route))
    (assert (= "routes.test.core" (:ns route)))
    (assert (= "address-route" (:name route)))
    (assert (= '[address] (route-args route)))
    (assert (= [[:location params/location]] (route-params route)))
    (assert (= "/addresses/%s" (route-pattern route)))
    (assert (= addresses-route (:root route)))
    (assert (= *server* (route-server route)))))

(defn test-address-path []
  (assert (= "/addresses/43.4073349,-2.6983217"
             (address-path address-of-mundaka))))

(defn test-address-url []
  (assert (= "https://example.com/addresses/43.4073349,-2.6983217"
             (address-url address-of-mundaka))))

(defn test-address-request []
  (let [request (address-request address-of-mundaka)]
    (assert (= :get (:request-method request)))
    (assert (= (address-path address-of-mundaka) (:uri request)))
    (assert (= (:server-name *server*) (:server-name request)))))

;; CONTINENTS

(defn test-continents-route []
  (let [route continents-route]
    (assert (route? route))
    (assert (= "routes.test.core" (:ns route)))
    (assert (= "continents-route" (:name route)))
    (assert (= [] (route-args route)))
    (assert (= [] (route-params route)))
    (assert (= "/continents" (route-pattern route)))
    (assert (= root-route (:root route)))
    (assert (= *server* (route-server route)))))

(defn test-continents-path []
  (assert (= "/continents" (continents-path))))

(defn test-continents-url []
  (assert (= "https://example.com/continents" (continents-url))))

(defn test-continents-request []
  (let [request (continents-request)]
    (assert (= :get (:request-method request)))
    (assert (= (continents-path) (:uri request)))
    (assert (= (:server-name *server*) (:server-name request)))))

;; CONTINENT

(defn test-continent-route []
  (let [route continent-route]
    (assert (route? route))
    (assert (= "routes.test.core" (:ns route)))
    (assert (= "continent-route" (:name route)))
    (assert (= ['continent] (route-args route)))
    (assert (= [[:id params/integer :name params/string]]
               (route-params route)))
    (assert (= "/continents/%s-%s" (route-pattern route)))
    (assert (= continents-route (:root route)))
    (assert (= *server* (route-server route)))))

(defn test-continent-path []
  (assert (= "/continents/1-europe" (continent-path europe))))

(defn test-continent-url []
  (assert (= "https://example.com/continents/1-europe" (continent-url europe))) )

(defn test-continent-request []
  (let [request (continent-request europe)]
    (assert (= :get (:request-method request)))
    (assert (= (continent-path europe) (:uri request)))
    (assert (= (:server-name *server*) (:server-name request)))))

;; COUNTRIES

(defn test-countries-route []
  (let [route countries-route]
    (assert (route? route))
    (assert (= "routes.test.core" (:ns route)))
    (assert (= "countries-route" (:name route)))
    (assert (= [] (route-args route)))
    (assert (= [] (route-params route)))
    (assert (= "/countries" (route-pattern route)))
    (assert (= root-route (:root route)))
    (assert (= *server* (route-server route)))))

(defn test-countries-path []
  (assert (= "/countries" (countries-path))))

(defn test-countries-url []
  (assert (= "https://example.com/countries" (countries-url))))

(defn test-countries-request []
  (let [request (countries-request)]
    (assert (= :get (:request-method request)))
    (assert (= (countries-path) (:uri request)))
    (assert (= (:server-name *server*) (:server-name request)))))

;; COUNTRIES OF CONTINENT

(defn test-countries-of-continent-route []
  (let [route countries-of-continent-route]
    (assert (route? route))
    (assert (= "routes.test.core" (:ns route)))
    (assert (= "countries-of-continent-route" (:name route)))
    (assert (= '[continent] (route-args route)))
    (assert (= [[:id params/integer :name params/string]]
               (route-params route)))
    (assert (= "/continents/%s-%s/countries" (route-pattern route)))
    (assert (= continent-route (:root route)))
    (assert (= *server* (route-server route)))))

(defn test-countries-of-continent-path []
  (assert (= "/continents/1-europe/countries"
             (countries-of-continent-path europe))))

(defn test-countries-of-continent-url []
  (assert (= "https://example.com/continents/1-europe/countries"
             (countries-of-continent-url europe))))

(defn test-countries-of-continent-request []
  (let [request (countries-of-continent-request europe)]
    (assert (= :get (:request-method request)))
    (assert (= (countries-of-continent-path europe) (:uri request)))
    (assert (= (:server-name *server*) (:server-name request)))))

;; COUNTRY OF CONTINENT #1

(defn test-country-of-continent-1-route []
  (let [route country-of-continent-1-route]
    (assert (route? route))
    (assert (= "routes.test.core" (:ns route)))
    (assert (= "country-of-continent-1-route" (:name route)))
    (assert (= '[continent country] (route-args route)))
    (assert (= [[:id params/integer :name params/string]
                [:iso-3166-1-alpha-2 params/iso-3166-1-alpha-2 :name params/string]]
               (route-params route)))
    (assert (= "/continents/%s-%s/countries/%s-%s" (route-pattern route)))
    (assert (= countries-of-continent-route (:root route)))
    (assert (= *server* (route-server route)))))

(defn test-country-of-continent-1-path []
  (assert (= "/continents/1-europe/countries/es-spain"
             (country-of-continent-1-path europe spain))))

(defn test-country-of-continent-1-url []
  (assert (= "https://example.com/continents/1-europe/countries/es-spain"
             (country-of-continent-1-url europe spain))))

(defn test-country-of-continent-1-request []
  (let [request (country-of-continent-1-request europe spain)]
    (assert (= :get (:request-method request)))
    (assert (= (country-of-continent-1-path europe spain) (:uri request)))
    (assert (= (:server-name *server*) (:server-name request)))))

;; COUNTRY OF CONTINENT #2

(defn test-country-of-continent-2-route []
  (let [route country-of-continent-2-route]
    (assert (route? route))
    (assert (= "routes.test.core" (:ns route)))
    (assert (= "country-of-continent-2-route" (:name route)))
    (assert (= '[continent country] (route-args route)))
    (assert (= "/continents/%s-%s/countries/%s-%s" (route-pattern route)))
    (assert (nil? (:root route)))
    (assert (= *server* (route-server route)))))

(defn test-country-of-continent-2-path []
  (assert (= "/continents/1-europe/countries/es-spain"
             (country-of-continent-2-path europe spain))))

(defn test-country-of-continent-2-url []
  (assert (= "https://example.com/continents/1-europe/countries/es-spain"
             (country-of-continent-2-url europe spain))))

(defn test-country-of-continent-2-request []
  (let [request (country-of-continent-2-request europe spain)]
    (assert (= :get (:request-method request)))
    (assert (= (country-of-continent-2-path europe spain) (:uri request)))
    (assert (= (:server-name *server*) (:server-name request)))))

(defn test []
  (test-root-route)
  (test-root-path)
  (test-root-url)
  (test-root-request)

  (test-addresses-route)
  (test-addresses-path)
  (test-addresses-url)
  (test-addresses-request)

  (test-address-route)
  (test-address-path)
  (test-address-url)
  (test-address-request)

  (test-continents-route)
  (test-continents-path)
  (test-continents-url)
  (test-continents-request)

  (test-continent-route)
  (test-continent-path)
  (test-continent-url)
  (test-continent-request)

  (test-countries-route)
  (test-countries-path)
  (test-countries-url)
  (test-countries-request)

  (test-countries-of-continent-route)
  (test-countries-of-continent-path)
  (test-countries-of-continent-url)
  (test-countries-of-continent-request)

  (test-country-of-continent-1-route)
  (test-country-of-continent-1-path)
  (test-country-of-continent-1-url)
  (test-country-of-continent-1-request)

  (test-country-of-continent-2-route)
  (test-country-of-continent-2-path)
  (test-country-of-continent-2-url)
  (test-country-of-continent-2-request))