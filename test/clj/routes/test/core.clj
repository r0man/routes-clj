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
    "/continents/:iso-3166-1-alpha-2-:name"))

(deftest test-continents-path []
  (is (= "/continents" (continents-path))))

(deftest test-continents-url []
  (is (= "https://example.com/continents" (continents-url))))

(deftest test-continents-route []
  (let [route (route :continents)]
    (is (= route (continents-route)))
    (is (instance? routes.helper.Route route))
    (is (= "/continents" (:pattern route)))
    (is (= [] (:args route)))
    (is (= :continents (:name route)))
    (is (= [] (:params route)))))

(deftest test-continent-path []
  (is (= "/continents/eu-europe" (continent-path europe))))

(deftest test-continent-url []
  (is (= "https://example.com/continents/eu-europe" (continent-url europe))) )

(deftest test-continent-route []
  (let [route (route :continent)]
    (is (= route (continent-route)))
    (is (instance? routes.helper.Route route))
    (is (= "/continents/:iso-3166-1-alpha-2-:name" (:pattern route)))
    (is (= ['continent] (:args route)))
    (is (= :continent (:name route)))
    (is (= [[:iso-3166-1-alpha-2 :name]] (:params route)))
    (is (= {:scheme :https, :server-name "example.com"} (:server route)))))
