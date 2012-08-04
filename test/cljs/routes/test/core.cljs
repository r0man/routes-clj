(ns routes.test.core
  (:use [routes.helper :only [route parse-url]]
        [routes.server :only [example *server*]])
  (:use-macros [routes.core :only [defroute with-server]]))

(def europe {:iso-3166-1-alpha-2 "eu" :name "Europe"})

(def spain {:iso-3166-1-alpha-2 "es" :name "Spain"})

(def address-of-mundaka
  {:location {:latitude 43.4073349 :longitude -2.6983217}})

(with-server example

  (defroute continents []
    "/continents")

  (defroute continent [continent]
    "/continents/[:iso-3166-1-alpha-2]-[:name]"
    :server *server*)

  (defroute languages []
    "/languages"
    :server "http://api.other.com")

  (defroute language [continent]
    "/languages/[:iso-639-1]-[:name]"
    :server {:scheme :https :server-name "api.other.com"}))

(defroute address [address]
  "/addresses/[:location :latitude],[:location :longitude]"
  :server example)

(defroute countries []
  "/countries" :server example)

;; ADDRESSES

(defn test-address-path []
  (assert (= "/addresses/43-4073349,2-6983217"
             (address-path address-of-mundaka))))

(defn test-address-url []
  (assert (= "https://example.com/addresses/43-4073349,2-6983217"
             (address-url address-of-mundaka))))

;; CONTINENTS

(defn test-continents-path []
  (assert (= "/continents" (continents-path))))

(defn test-continents-url []
  (assert (= "https://example.com/continents" (continents-url))))

(defn test-continents-route []
  (let [route (route :continents)]
    (assert (= route (continents-route)))
    (assert (instance? routes.helper.Route route))
    (assert (= "/continents" (:pattern route)))
    (assert (= [] (:args route)))
    (assert (= :continents (:name route)))
    (assert (= [] (:params route)))
    (assert (= example (:server route)))))

(defn test-continent-path []
  (assert (= "/continents/eu-europe" (continent-path europe))))

(defn test-continent-url []
  (assert (= "https://example.com/continents/eu-europe" (continent-url europe))) )

(defn test-continent-route []
  (let [route (route :continent)]
    (assert (= route (continent-route)))
    (assert (instance? routes.helper.Route route))
    (assert (= "/continents/%s-%s" (:pattern route)))
    (assert (= ['continent] (:args route)))
    (assert (= :continent (:name route)))
    (assert (= [[[:iso-3166-1-alpha-2] [:name]]] (:params route)))
    (assert (= {:scheme :https :server-name "example.com" :server-port 443} (:server route)))))

;; COUNTRIES

(defn test-countries-path []
  (assert (= "/countries" (countries-path))))

(defn test-countries-url []
  (assert (= "https://example.com/countries" (countries-url))))

(defn test-countries-route []
  (let [route (route :countries)]
    (assert (= route (countries-route)))
    (assert (instance? routes.helper.Route route))
    (assert (= "/countries" (:pattern route)))
    (assert (= [] (:args route)))
    (assert (= :countries (:name route)))
    (assert (= [] (:params route)))
    (assert (= example (:server route)))))

;; LANGUAGES

(defn test-languages-path []
  (assert (= "/languages" (languages-path))))

(defn test-languages-url []
  (assert (= "http://api.other.com/languages" (languages-url))))

(defn test-languages-route []
  (let [route (route :languages)]
    (assert (= route (languages-route)))
    (assert (instance? routes.helper.Route route))
    (assert (= "/languages" (:pattern route)))
    (assert (= [] (:args route)))
    (assert (= :languages (:name route)))
    (assert (= [] (:params route)))
    (assert (= (parse-url "http://api.other.com")
               (:server route)))))

(defn test []
  (test-address-path)
  (test-address-url)
  (test-continents-path)
  (test-continents-url)
  (test-continents-route)
  (test-continent-path)
  (test-continent-url)
  (test-continent-route)
  (test-countries-path)
  (test-countries-url)
  (test-countries-route)
  (test-languages-path)
  (test-languages-url)
  (test-languages-route))