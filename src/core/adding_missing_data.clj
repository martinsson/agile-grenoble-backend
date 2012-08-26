(ns core.adding-missing-data
  (:use core.program-import))

(def cleaned-sessions
  (filter-empty-line (sessions)))

(defn append-slot-to-header [csv]
  (cons (cons "Créneau | Slot" (first csv)) (rest csv)))

(defn append-slot-to-body [csv]
  (let [header (first csv)
        body   (rest csv)
        first-sessions (map #(cons 1 %) (take 2 body))
        second-sessions (map #(cons 2 %) (drop 2 body))] 
    (concat (list header) first-sessions second-sessions)))

(defn append-id [csv]
  (let [header (cons "id" (first csv))
        ids    (iterate inc 1)
        body   (map #(cons %1 %2) ids (rest csv))]
    (cons header body)))

(def decorated-sessions 
  (-> 
    cleaned-sessions
    (append-slot-to-header)
    (append-slot-to-body)
    (append-id)))

(defn sessions-with-missing-data [request]
  {:status 200
   :body decorated-sessions })