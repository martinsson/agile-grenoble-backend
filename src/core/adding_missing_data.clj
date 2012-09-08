(ns core.adding-missing-data
  (:use core.program-import))

(defn append-slot-to-header [csv]
  (cons (cons :slot (first csv)) (rest csv)))

(defn append-slot-to-body [csv]
  (let [header (first csv)
        body   (rest csv)
        ;TODO how about map-index below
        slot-distribution (map str (mapcat repeat (repeat 6) (range 1 9))) ;; get rid of this shit!!
        decorated-body (map cons slot-distribution body)] 
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

(def decorated-sessions 
  (decorate-sessions local-file))

(defn sessions-with-missing-data [request]
  {:status 200
   :body decorated-sessions })
