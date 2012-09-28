(ns core.program-import
  (:use midje.sweet
        [clojure-csv.core :only (parse-csv)])
  (:require [clj-json.core :as json])
  (:require [clojure.java.io :as io]))

(def key-dictionary {"id" :id
                     "slot" :slot
                     "Titre de la session | Title" :title
                     "Créneau | Slot" :slot
                     "Résume | Abstract" :abstract
                     "Quels bénéfices vont en retirer les participants ? | What will be the benefits for the participants?" :benefits
                     "Format | Format" :format
                     "Thèmes | Themes" :theme
                     "Prénom | First Name" :firstname
                     "Nom | Last Name" :lastname
                     "Retenu = x" :retained})

(defn normalize-headers [parsed-csv]
  (let [header              (first parsed-csv)
        body                (rest parsed-csv)
        replace-if-possible #(get-in key-dictionary [%] %)
        translated-header   (map replace-if-possible header)]
    (cons translated-header body)))

;; Used for testing
(def local-file (io/resource "public/sessions.csv"))



(defn normalized-sessions 
  ([csv-resource]
  (-> csv-resource
      slurp
      parse-csv
      normalize-headers)))

    (fact "The csv headers are normalized"
          (first (normalized-sessions local-file)) => (contains [:title :abstract :benefits :format :theme :firstname :lastname] :in-any-order :gaps-ok))
    (fact "The body is unchanged"
          (rest (normalized-sessions local-file))   => [...line1... 
                                             ...line2...]
          (provided (parse-csv anything) => [...header...
                                             ...line1...
                                             ...line2...]))
    (fact "it filters empty lines"
          (normalized-sessions local-file)   => [[:id :title] 
                                                 ["1" "kanban pour le mieux"]]
          (provided (parse-csv anything) => [["id" "Titre de la session | Title"]
                                             ["1"  "kanban pour le mieux"]]))
    
(defn sessions-as-maps [parsed-csv]
  (let [header (first parsed-csv)
        body   (rest parsed-csv)
        index-with-header (partial zipmap header)]
    (map index-with-header body)))

  ;TODO mock data
  (let [sessions (normalized-sessions local-file)] 
    (facts "transforms the cleaned csv to a list of maps, keys being the csv columns"
      (sessions-as-maps sessions) => (has every? #(% :title))
      (first (sessions-as-maps sessions)) => (contains {:title "Approche pragmatique pour industrialiser le développement d’applications"})
      (second (sessions-as-maps sessions)) => (contains {:title "Challenge Kanban"})))


(defn keep-retained [parsed-csv]
  (filter (comp not-empty :retained) (sessions-as-maps parsed-csv)))

(fact "retains only sessions marked as such"
      (keep-retained ..csv..) => [{:title "happy scrumming" :retained "x"}]
      (provided (sessions-as-maps ..csv..) => [{:title "happy XP" :retained ""}
                                              {:title "happy scrumming" :retained "x"}]))
