(ns routes.server
  (:require [routes.helper :refer [parse-url]]))

(def ^:dynamic *server* nil)

(def ^:dynamic *ports*
  {:http 80
   :https 443})

(def example
  {:scheme :https :server-name "example.com"})

(defn server-url
  "Returns the url of `server`."
  [server]
  (cond
   (string? server)
   (str (java.net.URL. server))
   (and (map? server) (:server-name server))
   (let [{:keys [scheme server-name server-port]} server]
     (str (name (or scheme :https)) "://" server-name
          (if (and server-port (not (= server-port (get *ports* scheme))))
            (str ":" server-port))))))

(defn wrap-server
  [handler]
  (fn [request]
    (binding [*server* request]
      (handler request))))
