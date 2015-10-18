(ns core.sessions-api
  (:use midje.sweet))

(defn session-list-for 
  ([session-maps slot]
  (let [reduce-keys #(select-keys % [:id :title :slot :room :speakers :type :length :theme :width])
        found-sessions (filter #(= slot (% :slot)) session-maps)] 
    (map reduce-keys found-sessions))))

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
     => (just [{:id ..id.. :title ..title.. :slot "3" :room ..room.. :speakers ..sl..}])
     :in-any-order)
  
