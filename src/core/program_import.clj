(ns core.program-import
  (:use clojure-csv.core midje.sweet)
  (:require [clj-json.core :as json]))

(def sessions 
  (parse-csv (slurp "resources/public/sessions.csv")))

(fact
  (first sessions) => (contains [#"Format.*"]))

(defn empty-csv-line? [line]
  (every? empty? line))

(fact
  (empty-csv-line? ["" "" ""]) => true 
  (empty-csv-line? ["" "a" ""]) => false )

(defn filter-empty-line [csv]
  (remove empty-csv-line? csv))

(fact 
  (second sessions) => empty-csv-line?
  (second (filter-empty-line sessions)) =not=> empty-csv-line?)

(defn get-session-3 [request]
  (let [line (nth sessions 3)
        header (first sessions)] 
    {:status 200
     :body (zipmap header line) }))


(defn append-slot-to-header [csv]
  (cons (cons "Créneau" (first csv)) (rest csv)))

(defn append-slot-to-body [csv]
  (list (first csv) (second csv)))
(facts
  (first (append-slot-to-header sessions)) => (has-prefix ["Créneau"])
  (second (append-slot-to-body (filter-empty-line sessions))) => (has-prefix ["8:30"])
  )