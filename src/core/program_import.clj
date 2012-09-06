(ns core.program-import
  (:use clojure-csv.core midje.sweet)
  (:require [clj-json.core :as json])
  (:require [clojure.java.io :as io]))

; TODO remove this as it isnt necessary in the latest version of sessions.csv    
(defn empty-csv-line? [line]
  (every? empty? line))

    (fact
      (empty-csv-line? ["" "" ""]) => true 
      (empty-csv-line? ["" "a" ""]) => false )

(defn filter-empty-line [csv]
  (remove empty-csv-line? csv))

    (fact 
      (let [sessions [["id" "title"]
                      [""   ""]
                      [1    "kanban pour le mieux"]]] 
        (second sessions) => empty-csv-line?
        (second (filter-empty-line sessions)) =not=> empty-csv-line?))


(def key-dictionary {"id" :id
                     "Titre de la session | Title" :title
                     "Créneau | Slot" :slot
                     "Résume | Abstract" :abstract
                     "Quels bénéfices vont en retirer les participants ? | What will be the benefits for the participants?" :benefits
                     "Format | Format" :format
                     "Thèmes | Themes" :theme
                     "Prénom | First Name" :firstname
                     "Nom | Last Name" :lastname})

(defn normalize-headers [parsed-csv]
  (let [header              (first parsed-csv)
        body                (rest parsed-csv)
        replace-if-possible #(get-in key-dictionary [%] %)
        translated-header   (map replace-if-possible header)]
    (cons translated-header body)))

(def local-file (io/resource "public/sessions.csv"))

(defn normalized-sessions 
  ([csv-resource]
  (-> csv-resource
      slurp
      parse-csv
      filter-empty-line
      normalize-headers)))

    (fact "The csv headers are normalized"
          (first (normalized-sessions local-file)) => (contains [:title :abstract :benefits :format :theme :firstname :lastname] :in-any-order :gaps-ok))
    (fact "The body is unchanged"
          (rest (normalized-sessions local-file))   => [...line1... 
                                             ...line2...]
          (provided (filter-empty-line anything) => [...header...
                                             ...line1...
                                             ...line2...]))
    (fact "it filters empty lines"
          (normalized-sessions local-file)   => [[:id :title] 
                                      ["1"    "kanban pour le mieux"]]
          (provided (parse-csv anything) => [["id" "Titre de la session | Title"]
                                             [""   ""]
                                             ["1"    "kanban pour le mieux"]]))
    

    