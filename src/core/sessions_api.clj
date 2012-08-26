(ns core.sessions-api
  (:use midje.sweet)
  (:require [core.adding-missing-data :as amd]))

(def time-slots ["8:30" "10:00" "11:00"])

(defn slot-list []
  (zipmap (iterate inc 1) 
          time-slots))

(facts
  (slot-list) => {1 "8:30" 
                  2 "10:00"
                  3 "11:00"})
