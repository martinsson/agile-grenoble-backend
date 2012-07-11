(defproject agile-grenoble-backend "1.0.0-SNAPSHOT"
  :description "backend serving session data for Agile Grenoble"
  :dependencies [[org.clojure/clojure "1.3.0"]
                   [compojure "1.1.0"]
                   [clj-json "0.5.1"]]
  :plugins [[lein-ring "0.7.1"]]
  :ring {:handler agile-grenoble-backend.core/app})
