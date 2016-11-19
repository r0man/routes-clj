(ns routes.core
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [no.en.core :as noencore]))

(defprotocol IRoute
  (route-matches [route request]))

(defrecord Route [name path-params path-parts path-re])
(defrecord Router [routes])

(defn- regex-source [r]
  (if r #?(:clj (str r) :cljs (.-source r))))

(defn parse-path-pattern
  "Parse the path pattern from the `s`."
  [s & [opts]]
  (let [[_ id _ pattern]
        (re-matches #":([^{]+)(\{(.+)\})?" (str s))
        custom (get opts (keyword id))]
    (if id
      [(keyword id)
       (str "(" (or pattern
                    (regex-source custom)
                    "[^/]+")
            ")")])))

(defn parse-path-component
  "Parse the path component from the `s`."
  [s & [opts]]
  (if-not (str/blank? (str s))
    (->> (str/split (str s) #"-(?=:)")
         (map #(or (parse-path-pattern % opts) %)))))

(defn parse-path
  "Parse the path from the `s`."
  [s & [opts]]
  (if-not (str/blank? (str s))
    (->> (str/split (str s) #"/")
         (map #(parse-path-component % opts)))))

(defn- compile-path-re
  "Compile the path regular expression from `parts`."
  [parts]
  (if (empty? parts)
    #"/"
    (->> (map (fn [components]
                (->> (map (fn [component]
                            (cond
                              (sequential? component)
                              (second component)
                              (string? component)
                              component
                              :else "/"))
                          components)
                     (str/join "-")))
              parts)
         (str/join "/")
         (re-pattern))))

(defn- compile-path-params
  "Compile the path params from `parts`."
  [parts]
  (->> (map (fn [components]
              (->> (map (fn [component]
                          (if (sequential? component)
                            (first component)))
                        components)
                   (remove nil? )
                   (vec)))
            parts)
       (vec)))

(defn- compile-path-parts
  "Compile the path params from `parts`."
  [parts]
  (->> (map (fn [components]
              (->> (map (fn [component]
                          (cond
                            (sequential? component)
                            (str (first component))
                            (string? component)
                            component))
                        components)
                   (str/join "-")))
            parts)
       (vec)))

(defn route-compile [route & [opts]]
  (if-let [parts (parse-path route opts)]
    (let [compiled (map->Route
                    {:path-params (compile-path-params parts)
                     :path-parts (compile-path-parts parts)
                     :path-re (compile-path-re parts)})]
      (if-not (set/subset? (set (keys opts))
                           (set (apply concat (:path-params compiled))))
        (throw (ex-info "Unused keys in regular expression map"
                        {:route route :opts opts})))
      compiled)))

(defn split-by-counts [counts coll]
  (let [[coll rest]
        (reduce
         (fn [[result coll] n]
           [(conj result (take n coll))
            (drop n coll)])
         [[] coll] counts)]
    (if (empty? rest)
      coll (conj coll rest))))

(defn path-matches [route matches]
  (map (fn [path-params matches]
         (if-not (empty? path-params)
           (zipmap path-params matches)))
       (:path-params route)
       (split-by-counts
        (map count (:path-params route))
        (next matches))))

(defn- path-info [request]
  (or (:path-info request)
      (:uri request)))

(defn- compiled-route-matches
  "Match the the compiled `route` against the `request`."
  [route request]
  (if-let [matches (re-matches (:path-re route) (path-info request))]
    (let [path-matches (path-matches route matches)]
      (merge request route
             {:path-matches path-matches
              :params (apply merge-with
                             (fn [x y]
                               (if (vector? x)
                                 (conj x y)
                                 [x y]))
                             path-matches)}))))

(defn compile-routes [routes]
  (vec (for [[pattern name] routes]
         (assoc (route-compile pattern)
                :name name))))

(extend-protocol IRoute
  Route
  (route-matches [route request]
    (compiled-route-matches route request))
  Router
  (route-matches [router request]
    (->> (:routes router)
         (map #(compiled-route-matches % request))
         (remove nil?)
         (first)))
  #?(:clj String :cljs string)
  (route-matches [route request]
    (route-matches (route-compile route) request)))

(defn find-route
  "Find the route with `name` in `routes`."
  [router name]
  (let [routes (:routes router)
        name (keyword name)]
    (first (filter #(= name (:name %)) routes))))

(defn split-args
  "Split the `args` interpolation arguments and options."
  [route args]
  (let [n (count (remove empty? (:path-params route)))]
    [(take n args) (drop n args)]))

(defn- path-args
  "Return the `args` for path interpolation."
  [route args]
  (let [args (first (split-args route args))]
    (-> (reduce
         (fn [m path-params]
           (if (empty? path-params)
             (update-in m [:path-args] conj nil)
             (-> (update-in m [:path-args] conj
                            (first (:pending m)))
                 (assoc :pending (next (:pending m))))))
         {:path-args [] :pending args}
         (:path-params route))
        :path-args)))

(defn- interpolate-path
  "Interpolate the path of `route` using `args`."
  [route args]
  (->> (map (fn [part params arg]
              (reduce
               (fn [part param]
                 (if-let [value (get arg param)]
                   (str/replace-first part (str param) value)
                   (throw (ex-info "Can't find path parameter."
                                   {:arg arg
                                    :param param
                                    :route route}))))
               part params))
            (:path-parts route args)
            (:path-params route args)
            (path-args route args))
       (str/join "/")))

(defn path-for
  "Find the route with `name` in `routes` and return the interpolated
  path using `args`."
  [router name & args]
  (if-let [route (find-route router name)]
    (let [[_ [query-params]] (split-args route args)
          uri (interpolate-path route args)]
      (noencore/format-url {:uri uri :query-params query-params}))))

(defn request-for
  "Find the route with `name` in `routes` and return a Ring request
  map, using `server` and any additional options."
  [router server name & args]
  (if-let [route (find-route router name)]
    (let [[_ [opts]] (split-args route args)]
      (assoc (merge {:scheme :http
                     :server-name "localhost"
                     :request-method :get}
                    server opts)
             :uri (interpolate-path route args)))))

(defn url-for
  "Find the route with `name` in `routes` and return the interpolated
  URL using `server` and `args`."
  [router server name & args]
  (if-let [route (find-route router name)]
    (let [[_ [query-params]] (split-args route args)]
      (-> (assoc server
                 :uri (interpolate-path route args)
                 :query-params query-params)
          (noencore/format-url)))))

#?(:clj (defmacro defroutes
          "Define routes."
          [name & routes]
          `(do (def ~name (routes.core/->Router ~(compile-routes routes)))
               (def ~'path-for (partial routes.core/path-for ~name))
               (def ~'request-for (partial routes.core/request-for ~name))
               (def ~'url-for (partial routes.core/url-for ~name)))))
