(ns core.program-import
  (:use clojure-csv.core midje.sweet)
  (:require [clj-json.core :as json]))

(def sessions 
  (parse-csv (slurp "resources/public/sessions.csv")))

(fact
  (first sessions) => (contains [#"Format.*"]))


(defn get-session-3 [request]
  (let [line (nth sessions 3)
        header (first sessions)] 
    {:status 200
     :body (zipmap header line) }))


(defn append-slot-header [csv]
  (cons (cons "Créneau" (first csv)) (rest csv)))

(fact
  (first (append-slot-header sessions)) => (has-prefix ["Créneau"]))