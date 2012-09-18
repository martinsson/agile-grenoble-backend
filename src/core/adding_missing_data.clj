(ns core.adding-missing-data
  (:use [core.program-import :only (normalized-sessions)]))

;; As there is no id, nor a chosen slot (at what time of the day the session will take place) yet
;; we artifically decorate the csv with these colums

(defn append-slot-to-header [csv]
  (cons (cons :slot (first csv)) 
        (rest csv)))

(defn append-slot-to-body [csv]
  (let [header (first csv)
        body   (rest csv)
        artificial-distribution (map str (mapcat (partial repeat 6) (range 1 9)))
        decorated-body (map cons artificial-distribution body)] 
    (concat (list header) decorated-body)))

(defn append-id [csv]
  (let [header (cons :id (first csv))
        ids    (map str (iterate inc 1))
        body   (map cons ids (rest csv))]
    (cons header body)))


(defn decorate-sessions 
  ([csv-resource] 
  (-> 
    (map reverse (normalized-sessions csv-resource))   ;; TODO remove hack to retain the first name/firstname when we make it into a map
    (append-slot-to-header)
    (append-slot-to-body)
    (append-id))))

