(defproject routes-clj "0.1.12-SNAPSHOT"
  :description "A Clojure & ClojureScript routing library."
  :url "https://github.com/r0man/routes-clj"
  :license {:name "Eclipse Public License"
            :url "https://www.eclipse.org/legal/epl-v10.html"}
  :author "r0man"
  :min-lein-version "2.6.1"
  :deploy-repositories [["releases" :clojars]]
  :dependencies [[noencore "0.3.3"]
                 [org.clojure/clojure "1.9.0-alpha14"]]  
  :plugins [[jonase/eastwood "0.2.3"]
            [lein-cljsbuild "1.1.4"]
            [lein-difftest "2.0.0"]
            [lein-doo "0.1.7"]]
  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"]]
                   :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
             :provided {:dependencies [[org.clojure/clojurescript "1.9.293"]]}}
  :aliases
  {"ci" ["do"
         ["clean"]
         ["difftest"]
         ["doo" "node" "node" "once"]
         ["doo" "phantom" "none" "once"]
         ["doo" "phantom" "advanced" "once"]
         ["lint"]]
   "lint" ["do"  ["eastwood"]]}
  :cljsbuild
  {:builds
   [{:id "none"
     :compiler
     {:main 'routes.test
      :optimizations :none
      :output-dir "target/none"
      :output-to "target/none.js"
      :parallel-build true
      :pretty-print true
      :verbose false}
     :source-paths ["src" "test"]}
    {:id "node"
     :compiler
     {:main 'routes.test
      :optimizations :none
      :output-dir "target/node"
      :output-to "target/node.js"
      :parallel-build true
      :pretty-print true
      :target :nodejs
      :verbose false}
     :source-paths ["src" "test"]}
    {:id "advanced"
     :compiler
     {:main 'routes.test
      :optimizations :advanced
      :output-dir "target/advanced"
      :output-to "target/advanced.js"
      :parallel-build true
      :pretty-print true
      :verbose false}
     :source-paths ["src" "test"]}]}  )
