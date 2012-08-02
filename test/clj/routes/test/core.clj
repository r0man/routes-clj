(ns routes.test.core
  (:use clojure.test
        routes.core
        routes.helper
        routes.server)
  (:import routes.helper.Route))

(def europe {:iso-3166-1-alpha-2 "eu" :name "Europe"})
(def spain {:iso-3166-1-alpha-2 "es" :name "Spain"})

(with-server example

  (defroute continents []
    "/continents")

  (defroute continent [continent]
    "/continents/:iso-3166-1-alpha-2-:name"
    :server *server*)

  (defroute languages []
    "/languages"
    :server "http://api.other.com")

  (defroute language [continent]
    "/languages/:iso-639-1-:name"
    :server {:scheme :https :server-name "api.other.com"}))

(defroute countries []
  "/countries" :server example)

;; CONTINENTS

(deftest test-continents-path
  (is (= "/continents" (continents-path))))

(deftest test-continents-url
  (is (= "https://example.com/continents" (continents-url))))

(deftest test-continents-route []
  (let [route (route :continents)]
    (is (= route (continents-route)))
    (is (instance? routes.helper.Route route))
    (is (= "/continents" (:pattern route)))
    (is (= [] (:args route)))
    (is (= :continents (:name route)))
    (is (= [] (:params route)))
    (is (= example (:server route)))))

(deftest test-continent-path
  (is (= "/continents/eu-europe" (continent-path europe))))

(deftest test-continent-url
  (is (= "https://example.com/continents/eu-europe" (continent-url europe))) )

(deftest test-continent-route
  (let [route (route :continent)]
    (is (= route (continent-route)))
    (is (instance? routes.helper.Route route))
    (is (= "/continents/:iso-3166-1-alpha-2-:name" (:pattern route)))
    (is (= ['continent] (:args route)))
    (is (= :continent (:name route)))
    (is (= [[:iso-3166-1-alpha-2 :name]] (:params route)))
    (is (= {:scheme :https, :server-name "example.com"} (:server route)))))

;; COUNTRIES

(deftest test-countries-path
  (is (= "/countries" (countries-path))))

(deftest test-countries-url
  (is (= "https://example.com/countries" (countries-url))))

(deftest test-countries-route
  (let [route (route :countries)]
    (is (= route (countries-route)))
    (is (instance? routes.helper.Route route))
    (is (= "/countries" (:pattern route)))
    (is (= [] (:args route)))
    (is (= :countries (:name route)))
    (is (= [] (:params route)))
    (is (= example (:server route)))))

;; LANGUAGES

(deftest test-languages-path
  (is (= "/languages" (languages-path))))

(deftest test-languages-url
  (is (= "http://api.other.com/languages" (languages-url))))

(deftest test-languages-route []
  (let [route (route :languages)]
    (is (= route (languages-route)))
    (is (instance? routes.helper.Route route))
    (is (= "/languages" (:pattern route)))
    (is (= [] (:args route)))
    (is (= :languages (:name route)))
    (is (= [] (:params route)))
    (is (= (parse-server "http://api.other.com")
           (:server route)))))
