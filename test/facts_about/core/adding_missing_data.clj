(ns facts_about.core.adding-missing-data
  (:use clojure-csv.core midje.sweet core.adding-missing-data))

(facts
  (first (append-slot-to-header cleaned-sessions)) => (has-prefix ["Créneau | Slot"]))

(facts
  (first (append-slot-to-body cleaned-sessions)) => (first cleaned-sessions)
  (second (append-slot-to-body cleaned-sessions)) => (has-prefix ["8:30"])
  (nth (append-slot-to-body cleaned-sessions) 2) => (has-prefix ["8:30"])
  (nth (append-slot-to-body cleaned-sessions) 3) => (has-prefix ["10:00"]))

(future-facts "handles more than two lines")