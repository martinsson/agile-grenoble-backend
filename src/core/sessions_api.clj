(ns core.sessions-api
  (:use midje.sweet)
  (:require [core.program-import :as pi]))

(def time-slots ["10:00" "10:15" "10:30" "10:45" "11:05" "11:20" "11:35" "11:50" "13:20" 
                 "13:45" "14:30"  "14:50" "15:05" "15:20" "15:35" "16:05" "16:20" "16:35" 
                 "16:50" "17:10" "17:25" "17:40" "17:55" "18:15"])
(def slot-list-basic (zipmap (iterate inc 1) time-slots))

(defn session-list-for 
  ([session-maps slot]
  (let [reduce-keys #(select-keys % [:id :title :slot :room :speakers :type :length :theme :width])
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
     => (just [{:id ..id.. :title ..title.. :slot "3" :room ..room.. :speakers ..sl.. :start-time "10:30"}])
     :in-any-order)
  
