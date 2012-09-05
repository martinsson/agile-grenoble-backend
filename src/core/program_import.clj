(ns core.program-import
  (:use clojure-csv.core midje.sweet)
  (:require [clj-json.core :as json])
  (:require [clojure.java.io :as io]))

(defn sessions []
  (parse-csv (slurp (io/resource "public/sessions.csv"))))

    (fact "the parsing works"
      (first (sessions)) => (contains [#"Format.*"]))

; TODO remove this as it isnt necessary in the latest version of sessions.csv    
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

