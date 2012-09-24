(ns facts_about.core.adding-missing-data
  (:use clojure-csv.core midje.sweet core.adding-missing-data core.program-import))

(let [sessions (normalized-sessions local-file)] 
  (facts
    (first (append-slot-to-header sessions)) => (has-prefix [:slot])
    (second (append-slot-to-header sessions)) => (second sessions))

  (facts
    (first (append-slot-to-body sessions)) => (first sessions)
    (second (append-slot-to-body sessions)) => (has-prefix "1")
    (nth (append-slot-to-body sessions) 2) => (has-prefix "1")
    (nth (append-slot-to-body sessions) 6) => (has-prefix "1")
    (nth (append-slot-to-body sessions) 7) => (has-prefix "2")
    (nth (append-slot-to-body sessions) 12) => (has-prefix "2")
    (nth (append-slot-to-body sessions) 13) => (has-prefix "3")))