(ns core.sessions-api
  (:use midje.sweet)
  (:require [core.adding-missing-data :as amd]))

;TODO rename to butterfly api

(def time-slots ["8:30" "10:00" "11:00" "14:00" "15:00" "16:30"])

(defn slot-list []
  (list (zipmap (iterate inc 1) 
                time-slots)))
  (facts
    (slot-list) => [{1 "8:30" 
                     2 "10:00"
                     3 "11:00"
                     4 "14:00"
                     5 "15:00"
                     6 "16:30"}])

(def key-dictionary {"id" :id
                     "Titre de la session | Title" :title
                     "Créneau | Slot" :slot
                     "Résume | Abstract" :abstract
                     "Quels bénéfices vont en retirer les participants ? | What will be the benefits for the participants?" :benefits
                     "Format | Format" :format
                     "Thèmes | Themes" :theme
                     "Prénom | First Name" :firstname
                     "Nom | Last Name" :lastname})

(defn normalize [raw-session] 
  (merge {:room "To be defined"} (clojure.set/rename-keys raw-session key-dictionary)))

  (fact "renames string keys to succint keywords"
        (normalize {"id" 55, "Créneau | Slot" 2, "Titre de la session | Title" "Kanban basics",
                    "Résume | Abstract" "les bases quoi", 
                    "Quels bénéfices vont en retirer les participants ? | What will be the benefits for the participants?" "savoir faire du kanban", 
                    "Format | Format" "Conférence", "Thèmes | Themes" "Process",
                    "Prénom | First Name" "Neil", "Nom | Last Name" "Armstrong"})
          => (contains {:id 55 :slot 2 :title "Kanban basics"
                        :abstract "les bases quoi",
                        :benefits "savoir faire du kanban",
                        :format "Conférence", :theme "Process"
                        :firstname "Neil", :lastname "Armstrong"}))
  (fact "adds key :room if not present"
        (normalize {:room "toto"}) => (contains {:room "toto"})
        (normalize {})             => (contains {:room "To be defined"}))
  (fact "does not filter any keys"
        (normalize {:somekey "somevalue"}) => (contains {:somekey "somevalue"}))

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
  (let [reduce-keys #(select-keys % [:id :title :slot :room])] 
    (map (comp reduce-keys normalize) (find-sessions-for slot))))

  (facts "finds, normalizes, and filters keys"
         (sessions-for ..slot..) => [{:id ..id.. :title ..title.. :slot ..slot.. :room ..room.. }]
         (provided (find-sessions-for ..slot..) => [..session..]
                   (normalize ..session..) => {"key to remove" "toto" 
                                                        :id ..id.. :title ..title.. :slot ..slot.. :room ..room.. }))
(defn sessions-as-autoindexed-maps [parsed-csv]
  (into {} (for [s (map normalize (sessions-as-maps parsed-csv))] {(:id s) s})))
  (facts 
    ;; TODO get rid of the room constraint
    (sessions-as-autoindexed-maps ..csv..) => {..id1.. {:id ..id1.. :title ..title1.. :room "To be defined"}
                                               ..id2.. {:id ..id2.. :title ..title2.. :room "To be defined"}}
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




