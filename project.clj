(defproject clj-cms "0.1.0-SNAPSHOT"
  :description "Simple CMS"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.6"]
                 [sonian/carica "1.1.0" :exclusions [[cheshire]]]
                 [korma "0.3.1"]
                 [mysql/mysql-connector-java "5.1.30"]
                 [fogus/ring-edn "0.2.0"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [enfocus "2.1.0-SNAPSHOT"]
                 [cljs-ajax "0.2.3"]]
  :plugins [[lein-ring "0.8.10"]]
  :profiles {:dev {:plugins [[lein-cljsbuild "1.0.3"]]}}
  :cljsbuild {
    :builds [{
        :source-paths ["src/"]
        :compiler {
          :output-to "resources/statis/js/main.js"
          :optimizations :whitespace
          :pretty-print true}}]}
  :ring {:handler clj-cms.web.core/app})
