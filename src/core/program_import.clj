(ns core.program-import
  (:use clojure-csv.core midje.sweet)
  (:require [clj-json.core :as json])
  (:require [clojure.java.io :as io]))

(defn sessions []
  (parse-csv (slurp (io/resource "public/sessions.csv"))))
(fact
  (first (sessions)) => (contains [#"Format.*"]))

(defn empty-csv-line? [line]
  (every? empty? line))

(fact
  (empty-csv-line? ["" "" ""]) => true 
  (empty-csv-line? ["" "a" ""]) => false )

(defn filter-empty-line [csv]
  (remove empty-csv-line? csv))

(fact 
  (second (sessions)) => empty-csv-line?
  (second (filter-empty-line (sessions))) =not=> empty-csv-line?)

(defn get-session-3 [request]
  (let [line (nth (sessions) 3)
        header (first (sessions))] 
    {:status 200
     :body (zipmap header line) }))


