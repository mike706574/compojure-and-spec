(defproject fun.mike/compojure-and-spec "0.0.1-SNAPSHOT"
  :description "An example of using compojure-api and spec together."
  :url "https://github.com/mike706574/compojure-and-spec"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/spec.alpha "0.2.168"]
                 [org.clojure/core.cache "0.7.1"]
                 [org.clojure/data.json "0.2.6"]
                 [com.stuartsierra/component "0.3.2"]

                 ;; Utility
                 [com.taoensso/timbre "4.10.0"]
                 [metosin/spec-tools "0.7.1"]

                 ;; Web
                 [aleph "0.4.6"]
                 [manifold "0.1.8"]
                 [ring/ring-anti-forgery "1.3.0"]
                 [ring-cors "0.1.12"]
                 [ring/ring-defaults "0.3.2"]
                 [metosin/compojure-api "2.0.0-alpha23"]
                 [metosin/ring-http-response "0.9.0"]

                 ;; Security
                 [buddy/buddy-hashers "1.3.0"]
                 [buddy/buddy-sign "2.2.0"]]
  :plugins [[org.clojure/tools.nrepl "0.2.12"]
            [lein-cloverage "1.0.10"]]
  :profiles {:dev {:source-paths ["dev"]
                   :target-path "target/dev"
                   :dependencies [[org.clojure/test.check "0.10.0-alpha2"]
                                  [org.clojure/tools.namespace "0.2.11"]]}
             :uberjar {:aot :all
                       :main example.main
                       :uberjar-name "example.jar"}})
