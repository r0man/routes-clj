(ns routes.test.param
  (:require [clojure.test :refer :all]
            [routes.param :refer :all]))

(deftest test-format-integer
  (is (= "1" (-format integer 1))))

(deftest test-format-iso-3166-1-alpha-2
  (is (= "us" (-format iso-3166-1-alpha-2 "USA"))))

(deftest test-format-iso-3166-1-alpha-3
  (is (= "usa" (-format iso-3166-1-alpha-3 "USA"))))

(deftest test-format-location
  (is (= "43.4073349,-2.6983217" (-format location {:latitude 43.4073349 :longitude -2.6983217}))))

(deftest test-format-string
  (is (=  "north-america" (-format string "North America"))))

(deftest test-parse-integer
  (is (= 1 (-parse integer "1"))))

(deftest test-parse-iso-3166-1-alpha-2
  (is (= "us" (-parse iso-3166-1-alpha-2 "us-united-states"))))

(deftest test-parse-iso-3166-1-alpha-3
  (is (= "usa" (-parse iso-3166-1-alpha-3 "usa-united-states"))))

(deftest test-parse-location
  (is (=  {:latitude 43.4073349 :longitude -2.6983217} (-parse location "43.4073349,-2.6983217"))))

(deftest test-parse-string
  (is (=  "north-america" (-parse string "north-america"))))
