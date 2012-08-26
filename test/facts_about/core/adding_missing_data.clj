(ns facts_about.core.adding-missing-data
  (:use clojure-csv.core midje.sweet core.adding-missing-data))

(facts
  (first (append-slot-to-header cleaned-sessions)) => (has-prefix ["Créneau | Slot"])
  (second (append-slot-to-header cleaned-sessions)) => (second cleaned-sessions))

(facts
  (first (append-slot-to-body cleaned-sessions)) => (first cleaned-sessions)
  (second (append-slot-to-body cleaned-sessions)) => (has-prefix 1)
  (nth (append-slot-to-body cleaned-sessions) 2) => (has-prefix 1)
  (nth (append-slot-to-body cleaned-sessions) 3) => (has-prefix 2))

(future-facts "handles more than two lines")