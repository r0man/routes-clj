(ns routes.server)

(def ^:dynamic *ports*
  {:http 80
   :https 443})

(defn server-url
  "Returns the url of `server`."
  [server]
  (cond
   (string? server)
   (str (java.net.URL. server))
   (and (map? server) (:server-name server))
   (let [{:keys [scheme server-name server-port]} server]
     (str (name (or scheme :https)) "://" server-name
          (cond )
          (if (and server-port (not (= server-port (get *ports* scheme))))
            (str ":" server-port))))))
