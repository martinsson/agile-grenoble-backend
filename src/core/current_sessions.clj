(ns core.current-sessions
  (:use midje.sweet)
  (:require [clj-time.core]
            [core.sessions-api :as sa]))

(def time-slots2 [[10 0] [11 10] [14 50] [16 10] [17 20]])

(def slot-list-basic (zipmap (iterate inc 1) sa/time-slots))

(defn current-slot-vec [[h m]]
  (let [make-time   (fn [[h m]] (clj-time.core/date-time 0 1 1 h m))
        now         (make-time [h m])] 
    (last (remove #(clj-time.core/before? now (make-time %)) time-slots2))))

(defn current-slot-id
  ([] (let [now (clj-time.core/now)] 
        (current-slot-id [(clj-time.core/hour now) (clj-time.core/minute now)])))
  ([[h m]]
    (let [inverted-slot-list (zipmap time-slots2 (iterate inc 1))] 
      (inverted-slot-list (current-slot-vec [h m])))))


(fact "returns the latest slot that has started or the first if none"
      (current-slot-vec [10 0]) => [10 0]
      (current-slot-vec [10 1]) => [10 0]
      (current-slot-vec [11 9]) => [10 0] 
      (current-slot-vec [11 10]) => [11 10] 
      (current-slot-vec [14 49]) => [11 10] 
      ;(current-slot-id [8 20]) => 1 
      (current-slot-id [10 00]) => 1 
      (current-slot-id [10 1]) => 1 
      (current-slot-id [11 9]) => 1 
      (current-slot-id [11 10]) => 2 
      (current-slot-id [14 49]) => 2 
      (current-slot-id [14 50]) => 3 
      (current-slot-id [16 9]) => 3 
      (current-slot-id [16 10]) => 4 
      (current-slot-id [17 20]) => 5 
      (current-slot-id [21 0]) => 5)

(defn upcoming-slot-vec [[h m]]
  (let [make-time   (fn [[h m]] (clj-time.core/date-time 0 1 1 h m))
        now         (make-time [h m])] 
    (first (filter #(clj-time.core/before? now (make-time %)) time-slots2))))

(defn upcoming-slot-id
  ([] (let [now (clj-time.core/now)] 
        (upcoming-slot-id [(clj-time.core/hour now) (clj-time.core/minute now)])))
  ([[h m]]
    (let [inverted-slot-list (zipmap time-slots2 (iterate inc 1))] 
      (inverted-slot-list (upcoming-slot-vec [h m])))))


(fact "returns the latest slot that has not yet started"
      (upcoming-slot-vec [10 0]) => [11 10]
      (upcoming-slot-vec [11 9]) => [11 10] 
      (upcoming-slot-vec [11 10]) => [14 50] 
      (upcoming-slot-vec [14 49]) => [14 50] 
      (upcoming-slot-id [8 20]) => 1 
      (upcoming-slot-id [10 00]) => 2 
      (upcoming-slot-id [10 1]) => 2 
      (upcoming-slot-id [11 9]) => 2 
      (upcoming-slot-id [11 10]) => 3 
      (upcoming-slot-id [14 49]) => 3 
      (upcoming-slot-id [14 50]) => 4
      (upcoming-slot-id [16 9]) => 4 
      (upcoming-slot-id [16 10]) => 5 
      ;(upcoming-slot-id [17 20]) => 5  
      ;(upcoming-slot-id [21 0]) => 5
      )