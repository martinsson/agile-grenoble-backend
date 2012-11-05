(ns core.sessions-api
  (:use midje.sweet)
  (:require [core.program-import :as pi]
            [clj-time.core]))

;TODO rename to butterfly api?

(def time-slots2 [[10 0] [11 10] [14 50] [16 10] [17 20]])
(def time-slots ["10:00" "11:10" "14:50" "16:10" "17:20"])

(def slot-list-basic (zipmap (iterate inc 1) time-slots))

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


(defn slot-list [](fact "returns the latest slot that has started or the first if none"
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
      (current-slot-id [21 0]) => 5 
      )

  (list slot-list-basic))
  (facts
    (slot-list) => [{1 "10:00", 2 "11:10", 3 "14:50", 4 "16:10", 5 "17:20"}])

(defn session-list-for 
  ([session-maps slot]
  (let [reduce-keys #(select-keys % [:id :title :slot :room :speakers :type])
        found-sessions (filter #(= slot (% :slot)) session-maps)] 
    (for [s (map reduce-keys found-sessions)]
              (assoc-in s [:start-time] (slot-list-basic (Integer/valueOf (:slot s)))) ))))

  (facts "find the sessions for a given slot"
    (session-list-for [{:slot 3} 
                   {:slot 2 :id 55}] 
                  2) => (just [(contains {:slot 2 :id 55})])
    (session-list-for [{:slot 3}
                   {:slot 4 :id 55}
                   {:slot 4 :id 77}] 
                  4) => (just [(contains {:slot 4 :id 55})
                               (contains {:slot 4 :id 77})]))

  (facts "filters keys"
     (session-list-for [{"key to remove" "toto" 
                     :id ..id.. :title ..title.. :slot "3" 
                     :room ..room.. :speakers ..sl..}] "3") 
     => (just [{:id ..id.. :title ..title.. :slot "3" :room ..room.. :speakers ..sl.. :start-time "14:50"}])
     :in-any-order)
  
(defn get-slot [session-maps slot]
  {:id slot 
   :start-time (slot-list-basic (Integer/valueOf slot)) 
   :sessions (session-list-for session-maps slot)})

  (facts "current-sessions provides an id, start-time and list of slots"  
    (get-slot ..maps.. "2") => (contains {:id "2" :start-time "11:10"})
    (get-slot ..maps.. "5") => (contains {:sessions ..sessions..})
    (provided (session-list-for ..maps.. "5") => ..sessions..))

(defn- get-session-simple 
  ([session-maps id]
  (first (filter #(= id (% :id)) session-maps))))

;; remove this wrapper when we handle the 120min sessions better
(defn get-session 
  ([session-maps id]
  (let [session (get-session-simple session-maps id)
        second-id  (:2nd-id session)] 
    (if (empty? second-id)
      session
      (get-session-simple session-maps second-id) ;it is a 2nd part session, get the real one
      ))))


  (facts "it gets a session based on its id"
    (get-session [{:id ..id.. :title "Hej"}] ..id..) => {:id ..id.. :title "Hej"}
    (get-session [{:id ..id1.. :title "Hej"}
                  {:id ..id2.. :title "Hopp"}
                  {:id ..id3.. :title "Daa"}] ..id2..) => {:id ..id2.. :title "Hopp"})
  
  (fact "it handles the 2nd part sessions"
    (get-session [{:id "22" :title "le must"}
                  {:id "99" :2nd-id "22"}] "99") => {:id "22" :title "le must"}
    (get-session [{:id "22" :title "le must"}
                  {:id "99" :2nd-id "" :title "CI for dummies"}] "99") => (contains {:id "99" :title "CI for dummies"}))

