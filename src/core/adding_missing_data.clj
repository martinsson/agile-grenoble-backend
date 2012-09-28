(ns core.adding-missing-data
  (:use [core.program-import :only (normalized-sessions)]))

;; As there is no id, nor a chosen slot (at what time of the day the session will take place) yet
;; we artifically decorate the csv with these colums


(defn decorate-sessions 
  ([csv-resource] 
  (-> 
    (map reverse (normalized-sessions csv-resource))   ;; TODO remove hack to retain the first name/firstname when we make it into a map
)))

