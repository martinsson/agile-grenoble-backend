(ns core.adding-missing-data
  (:use core.program-import))

(def cleaned-sessions
  (filter-empty-line sessions))


(defn append-slot-to-header [csv]
  (cons (cons "Créneau" (first csv)) (rest csv)))

(defn append-slot-to-body [csv]
  (let [header (first csv)
        body   (rest csv)
        first-sessions (map #(cons "8:30" %) (take 2 body))
        second-sessions (map #(cons "10:00" %) (drop 2 body))
        ] 
    (concat '(header) first-sessions second-sessions)))
(facts
  (first (append-slot-to-header sessions)) => (has-prefix ["Créneau"])
  (second (append-slot-to-body cleaned-sessions)) => (has-prefix ["8:30"])
  (nth (append-slot-to-body cleaned-sessions) 2) => (has-prefix ["8:30"])
  (nth (append-slot-to-body cleaned-sessions) 3) => (has-prefix ["10:00"])
  
  )