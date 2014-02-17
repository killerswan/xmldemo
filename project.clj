(defproject om-tut "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2156"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [om "0.4.2"]
                 [com.facebook/react "0.8.0.1"]
                 [ring "1.2.1"]]

  :plugins [[lein-cljsbuild "1.0.2"]
            [lein-ring "0.8.10"]]

  :hooks [leiningen.cljsbuild]

  :source-paths ["src"]

  :cljsbuild {
    :builds [{:id "xmldemo"
              :source-paths ["src"]
              :compiler {
                :output-to "www/app.js"
                :output-dir "www/app"
                :optimizations :none
                :source-map true}}]}

  :ring {:handler ringapp/app})
