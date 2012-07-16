(ns routes.server
  (:require [routes.helper :refer [parse-url]]))

(def ^:dynamic *server* nil)

(def example
  {:scheme :https :server-name "example.com"})

(defn parse-server [server]
  (if (string? server)
    (parse-url server) server))

(defn server-url
  "Returns the url of `server`."
  [{:keys [scheme server-name server-port] :as server}]
  (assert server-name "Can't build server url without a server name")
  (str (name (or scheme :https)) "://" server-name
       (if server-port (str ":" server-port))))
