(ns core.adding-missing-data
  (:use core.program-import))

(def cleaned-sessions
  (filter-empty-line (sessions)))

(defn append-slot-to-header [csv]
  (cons (cons "Créneau | Slot" (first csv)) (rest csv)))

(defn append-slot-to-body [csv]
  (let [header (first csv)
        body   (rest csv)
        slot-distribution (mapcat repeat (repeat 6) (range 1 9)) ;; get rid of this shit!!
        decorated-body (map #(cons %1 %2) slot-distribution body)] 
    (concat (list header) decorated-body)))

(defn append-id [csv]
  (let [header (cons "id" (first csv))
        ids    (iterate inc 1)
        body   (map #(cons %1 %2) ids (rest csv))]
    (cons header body)))

(def decorated-sessions 
  (-> 
    (map reverse cleaned-sessions)   ;; TODO remove hack to retain the first name/firstname when we make it into a map
    (append-slot-to-header)
    (append-slot-to-body)
    (append-id)))

(defn sessions-with-missing-data [request]
  {:status 200
   :body decorated-sessions })
(defn sessions-with-missing-data [request]
  {:status 200
   :body decorated-sessions })