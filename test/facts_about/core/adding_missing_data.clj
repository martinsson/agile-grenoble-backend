(ns facts_about.core.adding-missing-data
  (:use clojure-csv.core midje.sweet core.adding-missing-data))

(facts
  (first (append-slot-to-header cleaned-sessions)) => (has-prefix ["Créneau | Slot"])
  (second (append-slot-to-header cleaned-sessions)) => (second cleaned-sessions))

(facts
  (first (append-slot-to-body cleaned-sessions)) => (first cleaned-sessions)
  (second (append-slot-to-body cleaned-sessions)) => (has-prefix 1)
  (nth (append-slot-to-body cleaned-sessions) 2) => (has-prefix 1)
  (nth (append-slot-to-body cleaned-sessions) 6) => (has-prefix 1)
  (nth (append-slot-to-body cleaned-sessions) 7) => (has-prefix 2)
  (nth (append-slot-to-body cleaned-sessions) 12) => (has-prefix 2)
  (nth (append-slot-to-body cleaned-sessions) 13) => (has-prefix 3)
  )

(facts
  (first (append-id cleaned-sessions)) => (has-prefix ["id"])
  (second (append-id cleaned-sessions)) => (has-prefix [1]))