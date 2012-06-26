(ns routes.test.core
  (:use [routes.helper :only [format-pattern parse-keys route]]
        [routes.server :only [*server* example-server server-url]])
  (:use-macros [routes.core :only [defroute with-server]]))

(def europe {:iso-3166-1-alpha-2 "eu" :name "Europe"})
(def spain {:iso-3166-1-alpha-2 "es" :name "Spain"})

(defn test-defroute []
  (defroute continents []
    "/continents")
  (with-server example-server
    (assert (= "/continents" (continents-path)))
    (assert (= "https://example.com/continents" (continents-url)))
    (let [route (route :continents)]
      (assert (= route (continents-route)))
      (assert (instance? routes.helper.Route route))
      (assert (= "/continents" (:pattern route)))
      (assert (= [] (:args route)))
      (assert (= :continents (:name route)))
      (assert (= [] (:params route)))))
  (defroute continent [continent]
    "/continents/:iso-3166-1-alpha-2-:name")
  (with-server example-server
    (assert (= "/continents/eu-europe" (continent-path europe)))
    (assert (= "https://example.com/continents/eu-europe" (continent-url europe)))
    (let [route (route :continent)]
      (assert (= route (continent-route)))
      (assert (instance? routes.helper.Route route))
      (assert (= "/continents/:iso-3166-1-alpha-2-:name" (:pattern route)))
      (assert (= ['continent] (:args route)))
      (assert (= :continent (:name route)))
      (assert (= [[:iso-3166-1-alpha-2 :name]] (:params route))))))

(defn test []
  (test-defroute))