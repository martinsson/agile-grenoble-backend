(ns core.sessions-api
  (:use midje.sweet)
  (:require [core.adding-missing-data :as amd]))

;TODO rename to butterfly api

(def time-slots ["8:30" "10:00" "11:00"])

(defn slot-list []
  (zipmap (iterate inc 1) 
          time-slots))

(facts
  (slot-list) => {1 "8:30" 
                  2 "10:00"
                  3 "11:00"})

(defn normalized-session [raw-session] 
  (let [key-dictionary {"id" :id
                        "Titre de la session | Title" :title
                        "Créneau | Slot" :slot}]
    (merge {:room nil} (clojure.set/rename-keys raw-session key-dictionary))))

(fact "renames string keys to succint keywords"
      (normalized-session {"id" 55 "Créneau | Slot" 2 "Titre de la session | Title" "Kanban basics" })
        => (contains {:id 55 :slot 2 :title "Kanban basics"}))
(fact "adds key :room if not present"
      (normalized-session {:room "toto"}) => (contains {:room "toto"})
      (normalized-session {})             => (contains {:room nil}))
(fact "does not filter any keys"
      (normalized-session {:somekey "somevalue"}) => (contains {:somekey "somevalue"}))

(defn- sessions-as-maps [parsed-csv]
  (let [header (first parsed-csv)
        body   (rest parsed-csv)
        index-with-header (partial zipmap header)]
    (map index-with-header body)))

;TODO mock data
(facts "transforms the cleaned csv to a list of maps, keys being the csv columns"
  (sessions-as-maps amd/cleaned-sessions) => (has every? #(% "Titre de la session | Title"))
  (first (sessions-as-maps amd/cleaned-sessions)) => (contains {"Titre de la session | Title" "Approche pragmatique pour industrialiser le développement d’applications"})
  (second (sessions-as-maps amd/cleaned-sessions)) => (contains {"Titre de la session | Title" "Challenge Kanban"}))

(defn find-sessions-for [slot]
  (for [s (sessions-as-maps amd/decorated-sessions) :when (= slot (s "Créneau | Slot"))] s))

(facts "find the sessions for a given slot"
  (find-sessions-for 2) => [{"Créneau | Slot" 2 "id" 55}]
    (provided (sessions-as-maps amd/decorated-sessions) => 
              [{"Créneau | Slot" 3}
               {"Créneau | Slot" 2 "id" 55}])
  (find-sessions-for 4) => [{"Créneau | Slot" 4 "id" 55}
                       {"Créneau | Slot" 4 "id" 77}]
    (provided (sessions-as-maps amd/decorated-sessions) => 
              [{"Créneau | Slot" 3}
               {"Créneau | Slot" 4 "id" 55}
               {"Créneau | Slot" 4 "id" 77}]))

(defn sessions-for [slot]
  (map #(select-keys % [:id :title :slot :room]) (map normalized-session (find-sessions-for slot))))

(facts "finds, normalizes, and filters keys"
       (sessions-for ..slot..) => [{:id ..id.. :title ..title.. :slot ..slot.. :room ..room.. }]
       (provided (find-sessions-for ..slot..) => [..session..]
                 (normalized-session ..session..) => {"key to remove" "toto" 
                                                      :id ..id.. :title ..title.. :slot ..slot.. :room ..room.. }))





