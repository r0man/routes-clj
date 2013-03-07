(defproject routes-clj "0.0.2-SNAPSHOT"
  :description "A Clojure & ClojureScript library to build url and path fns."
  :url "http://github.com/r0man/routes-clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[inflections "0.8.0"]
                 [org.clojure/clojure "1.5.0"]]
  :profiles {:dev {:dependencies [[com.cemerick/clojurescript.test "0.0.1"]]}}
  :plugins [[lein-cljsbuild "0.3.0"]]
  :hooks [leiningen.cljsbuild]
  :cljsbuild {:builds [{:compiler {:output-to "target/routes-test.js"
                                   :optimizations :whitespace
                                   :pretty-print true}
                        :source-paths ["test"]}
                       {:compiler {:output-to "target/routes-debug.js"
                                   :optimizations :whitespace
                                   :pretty-print true}
                        :source-paths ["src"]}
                       {:compiler {:output-to "target/routes.js"
                                   :optimizations :advanced
                                   :pretty-print true}
                        :source-paths ["src"]
                        :jar true}]
              :crossover-jar true
              :crossover-path ".crossover-cljs"
              :crossovers [routes.helper
                           routes.params
                           routes.server]
              :repl-listen-port 9000
              :repl-launch-commands
              {"chromium" ["chromium" "http://localhost:9000/"]
               "firefox" ["firefox" "http://http://localhost:9000/"]}
              :test-commands {"unit-tests" ["runners/phantomjs.js" "target/routes-test.js"]}})
