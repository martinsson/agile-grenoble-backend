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
                        "Créneau | Slot" :slot}
        normalized-session (merge {:room nil} (clojure.set/rename-keys raw-session key-dictionary))]
    (select-keys normalized-session [:room :id :title :slot])))

(facts "adds key :room if not present, renames string keys to succint keywords"
  (normalized-session {"Créneau | Slot" 2 
                       "Titre de la session | Title" "Kanban basics" 
                       "id" 55}) => {:slot 2
                                     :title "Kanban basics" 
                                     :id 55 
                                     :room nil})

(defn sessions-for [slot]
  (for [s (sessions-as-maps amd/cleaned-sessions) :when (= slot (s "Créneau | Slot"))] s))

(facts "find the sessions for a given slot"
  (sessions-for 2) => [{"Créneau | Slot" 2 "id" 55}]
    (provided (sessions-as-maps amd/cleaned-sessions) => 
              [{"Créneau | Slot" 3}
               {"Créneau | Slot" 2 "id" 55}])
  (sessions-for 4) => [{"Créneau | Slot" 4 "id" 55}
                       {"Créneau | Slot" 4 "id" 77}]
    (provided (sessions-as-maps amd/cleaned-sessions) => 
              [{"Créneau | Slot" 3}
               {"Créneau | Slot" 4 "id" 55}
               {"Créneau | Slot" 4 "id" 77}])
  )



(defn sessions-as-maps [parsed-csv]
  (let [header (first parsed-csv)
        body   (rest parsed-csv)
        index-with-header (partial zipmap header)]
    (map index-with-header body)))

;TODO mock data
(facts "transforms the cleaned csv to a list of maps, keys being the csv columns"
  (sessions-as-maps amd/cleaned-sessions) => (has every? #(% "Titre de la session | Title"))
  (first (sessions-as-maps amd/cleaned-sessions)) => (contains {"Titre de la session | Title" "Approche pragmatique pour industrialiser le développement d’applications"})
  (second (sessions-as-maps amd/cleaned-sessions)) => (contains {"Titre de la session | Title" "Challenge Kanban"}))


