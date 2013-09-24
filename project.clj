(defproject agile-grenoble-backend "1.0.0-SNAPSHOT"
  :description "backend serving session data for Agile Grenoble"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [ring/ring-core "1.2.0"]
                   [com.cemerick/friend "0.1.5"]
                   [clj-json "0.5.1"]
                   [clojure-csv/clojure-csv "2.0.0-alpha2"]
                   [midje "1.4.0"]
                   [enlive "1.0.1"]
                   [hiccup "1.0.1"]
                   [compojure "1.1.5" :exclusions [ring/ring-core]]
                   ]
  :dev-dependencies [[lein-midje "1.0.10"]
                     [ring/ring-core "1.2.0"]] 
  :plugins [[lein-ring "0.7.1"]]
  :ring {:handler infra.http/app})


