(defproject routes-clj "0.0.1-SNAPSHOT"
  :description "A Clojure & ClojureScript library to build url and path fns."
  :url "http://github.com/r0man/routes-clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[inflections "0.7.0-SNAPSHOT"]
                 [org.clojure/clojure "1.4.0"]]
  :plugins [[lein-cljsbuild "0.2.1"]]
  :hooks [leiningen.cljsbuild]
  :cljsbuild {:builds [{:source-path "src/cljs"
                        :compiler {:output-to "target/routes-debug.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}
                       {:compiler {:output-to "target/routes.js"
                                   :optimizations :advanced
                                   :pretty-print false}
                        :source-path "src/cljs"}
                       {:compiler {:output-to "target/routes-test.js"
                                   :optimizations :advanced
                                   :pretty-print true}
                        :jar true
                        :source-path "test/cljs"}]
              :crossover-jar true
              :crossover-path ".crossover-cljs"
              :crossovers [routes.helper]
              :repl-listen-port 9000
              :repl-launch-commands
              {"chromium" ["chromium" "http://localhost:9000/"]
               "firefox" ["firefox" "http://http://localhost:9000/"]}
              :test-commands {"unit" ["./test-cljs.sh"]}}
  :source-paths ["src/clj"]
  :test-paths ["test/clj"])