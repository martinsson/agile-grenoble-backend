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

(defn sessions-for [slot]
  )

(future-facts 
  (sessions-for 2) => {:id 3 
                       :title "Duo de retour d’expérience sauce aigre douce " 
                       :room nil})

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