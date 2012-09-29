(ns core.sessions-api
  (:use midje.sweet)
  (:require [core.program-import :as pi]))

;TODO rename to butterfly api?

(def time-slots ["8:30" "10:00" "11:00" "14:00" "15:00" "16:30"])

(defn slot-list []
  (list (zipmap (iterate inc 1) 
                time-slots)))
  (facts
    (slot-list) => [{1 "8:30", 2 "10:00", 3 "11:00", 4 "14:00", 5 "15:00", 6 "16:30"}])

(defn sessions-for 
  ([session-maps slot]
  (let [reduce-keys #(select-keys % [:id :title :slot :room :speakers])
        found-sessions (filter #(= slot (% :slot)) session-maps)] 
    (map reduce-keys found-sessions))))

  (facts "find the sessions for a given slot"
    (sessions-for [{:slot 3} 
                   {:slot 2 :id 55}] 
                  2) => [{:slot 2 :id 55}]
    (sessions-for [{:slot 3}
                   {:slot 4 :id 55}
                   {:slot 4 :id 77}] 
                  4) => [{:slot 4 :id 55}
                         {:slot 4 :id 77}])

  (facts "filters keys"
     (sessions-for [{"key to remove" "toto" 
                     :id ..id.. :title ..title.. :slot ..slot.. :room ..room.. :speakers ..sl..}] ..slot..) 
     => [{:id ..id.. :title ..title.. :slot ..slot.. :room ..room.. :speakers ..sl.. }])
  
(defn get-session 
  ([session-maps id]
  (first (filter #(= id (% :id)) session-maps))))

  (facts
    (get-session [{:id ..id.. :title "Hej"}] ..id..) => {:id ..id.. :title "Hej"}
    (get-session [{:id ..id1.. :title "Hej"}
                  {:id ..id2.. :title "Hopp"}
                  {:id ..id3.. :title "Daa"}] ..id2..) => {:id ..id2.. :title "Hopp"})

  (future-fact "we add a room column")
;TODO add :room


