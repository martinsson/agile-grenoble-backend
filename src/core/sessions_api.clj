(ns core.sessions-api
  (:use midje.sweet)
  (:require [core.program-import :as pi]))

;TODO rename to butterfly api?

(def time-slots ["10:00" "11:10" "14:50" "16:10" "17:20"])

(defn slot-list []
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

