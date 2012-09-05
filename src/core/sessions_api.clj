(ns core.sessions-api
  (:use midje.sweet)
  (:require [core.adding-missing-data :as amd]
            [core.program-import :as pi]))

;TODO rename to butterfly api

(def time-slots ["8:30" "10:00" "11:00" "14:00" "15:00" "16:30"])

(defn slot-list []
  (list (zipmap (iterate inc 1) 
                time-slots)))
  (facts
    (slot-list) => [{1 "8:30", 2 "10:00", 3 "11:00", 4 "14:00", 5 "15:00", 6 "16:30"}])

(defn- sessions-as-maps [parsed-csv]
  (let [header (first parsed-csv)
        body   (rest parsed-csv)
        index-with-header (partial zipmap header)]
    (map index-with-header body)))

  ;TODO mock data
  (let [sessions (pi/normalized-sessions)] 
    (facts "transforms the cleaned csv to a list of maps, keys being the csv columns"
      (sessions-as-maps sessions) => (has every? #(% :title))
      (first (sessions-as-maps sessions)) => (contains {:title "Approche pragmatique pour industrialiser le développement d’applications"})
      (second (sessions-as-maps sessions)) => (contains {:title "Challenge Kanban"})))

(defn- find-sessions-for [slot]
  (for [s (sessions-as-maps amd/decorated-sessions) :when (= slot (:slot s))] s))

  (facts "find the sessions for a given slot"
    (find-sessions-for 2) => [{:slot 2 "id" 55}]
      (provided (sessions-as-maps amd/decorated-sessions) => 
                [{:slot 3}
                 {:slot 2 "id" 55}])
    (find-sessions-for 4) => [{:slot 4 "id" 55}
                         {:slot 4 "id" 77}]
      (provided (sessions-as-maps amd/decorated-sessions) => 
                [{:slot 3}
                 {:slot 4 "id" 55}
                 {:slot 4 "id" 77}]))

(defn sessions-for [slot]
  (let [reduce-keys #(select-keys % [:id :title :slot :room])] 
    (map reduce-keys (find-sessions-for slot))))

  (facts "finds, and filters keys"
         (sessions-for ..slot..) => [{:id ..id.. :title ..title.. :slot ..slot.. :room ..room.. }]
         (provided (find-sessions-for ..slot..) => [{"key to remove" "toto" 
                                                        :id ..id.. :title ..title.. :slot ..slot.. :room ..room..}]
                   ))
(defn sessions-as-autoindexed-maps [parsed-csv]
  (into {} (for [s (sessions-as-maps parsed-csv)] {(:id s) s})))
  (facts 
    ;; TODO get rid of the room constraint
    (sessions-as-autoindexed-maps ..csv..) => {..id1.. {:id ..id1.. :title ..title1.. }
                                               ..id2.. {:id ..id2.. :title ..title2.. }}
    (provided (sessions-as-maps ..csv..) => [{:id ..id1.. :title ..title1..}
                                             {:id ..id2.. :title ..title2..}]))

(defn get-session [id]
  ((sessions-as-autoindexed-maps amd/decorated-sessions) id))

  (facts
    (get-session 1) => ..session..
    (provided (sessions-as-autoindexed-maps amd/decorated-sessions) => {1 ..session..})
    (get-session 3) => ..session3..
    (provided (sessions-as-autoindexed-maps amd/decorated-sessions) => {1 ..session1..
                                                            2 ..session2..
                                                            3 ..session3..}))

  
(defn get-session2 [id]
  ((sessions-as-autoindexed-maps (pi/normalized-sessions)) id))

  (facts
    (get-session2 ..id..) => {:id ..id.. :title "Hej"}
    (provided (pi/normalized-sessions) => [[:id :title]
                                           [..id.. "Hej"]])
    )



