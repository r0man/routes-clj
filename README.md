# ROUTES-CLJ
  [![Build Status](https://travis-ci.org/r0man/routes-clj.png)](https://travis-ci.org/r0man/routes-clj)
  [![Dependencies Status](http://jarkeeper.com/r0man/routes-clj/status.png)](http://jarkeeper.com/r0man/routes-clj)

A Clojure & ClojureScript routing library.

## Installation

[![Current Version](https://clojars.org/routes-clj/latest-version.svg)](https://clojars.org/routes-clj)

## Usage

```
(require '[routes.core :refer :all])

(defroutes my-routes
  [{:route-name :countries
    :path-re #"/countries"
    :method :get
    :path "/countries"
    :path-parts ["" "countries"]
    :path-params []}
   {:route-name :country
    :path-re #"/countries/([^/]+)"
    :method :get
    :path-constraints {:id "([^/]+)"}
    :path "/countries/:id"
    :path-parts ["" "countries" :id]
    :path-params [:id]}])

(path-for :countries)
;=> "/countries"

(path-for :country {:path-params {:id 1}})
;=> "/countries/1"

(def server
  {:scheme :https
   :server-name "example.com"
   :server-port 443})

(url-for server :countries)
;=> "https://example.com/countries"

(url-for server :country {:path-params {:id 1}})
;=> "https://example.com/countries/1"
```

## License

Copyright © 2014 r0man

Distributed under the Eclipse Public License, the same as Clojure.
