(ns routes.test.core
  (:use [routes.helper :only [route]]
        [routes.server :only [example]])
  (:use-macros [routes.core :only [defroute with-server]]))

(def europe {:iso-3166-1-alpha-2 "eu" :name "Europe"})
(def spain {:iso-3166-1-alpha-2 "es" :name "Spain"})

(with-server example

  (defroute continents []
    "/continents")

  (defroute continent [continent]
    "/continents/:iso-3166-1-alpha-2-:name"))

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
    (assert (= [] (:params route)))))

(defn test-continent-path []
  (assert (= "/continents/eu-europe" (continent-path europe))))

(defn test-continent-url []
  (assert (= "https://example.com/continents/eu-europe" (continent-url europe))) )

(defn test-continent-route []
  (let [route (route :continent)]
    (assert (= route (continent-route)))
    (assert (instance? routes.helper.Route route))
    (assert (= "/continents/:iso-3166-1-alpha-2-:name" (:pattern route)))
    (assert (= ['continent] (:args route)))
    (assert (= :continent (:name route)))
    (assert (= [[:iso-3166-1-alpha-2 :name]] (:params route)))
    (assert (= {:scheme :https, :server-name "example.com"} (:server route)))))

(defn test []
  (test-continents-path)
  (test-continents-url)
  (test-continents-route)
  (test-continent-path)
  (test-continent-url)
  (test-continent-route))