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
                     "Retenu = x" :retained
                     "room" :room})

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
          (first (normalized-sessions local-file)) => (contains [:title :abstract :benefits :format :theme :firstname :lastname :room] :in-any-order :gaps-ok))
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

;; yuck! maybe it would be better to parse columns instead of lines
;; maybe I could extract some higher level logic like the fact that a csv has header/body,
;; that we insert something in the header/body like assoc-in, update-in. Perhaps protocols?
(defn assemble-speakers [normalized-csv] 
  (let [body           (rest normalized-csv)
        header         (first normalized-csv)
        indexes-of (fn [e] (keep-indexed #(if (= e %2) %1) header))
        firstnames-pos (indexes-of :firstname)
        lastnames-pos (indexes-of :lastname)
        speaker-names (fn [line] (map #(str (nth line %) " " (nth line %2)) firstnames-pos lastnames-pos))
        filter-blank (partial filter (complement clojure.string/blank?))]
    (cons (cons :speakers header) 
          (map #(cons (filter-blank (speaker-names %)) %) body))))
          
    (fact "makes a speaker-list"
          (assemble-speakers [[:id :firstname :lastname] 
                              ["1" "Alexandre" "Boutin"]
                              ["4" "Manuel" "Vacelet"]])   => [[:speakers :id :firstname :lastname]
                                                                 [["Alexandre Boutin"] "1"  "Alexandre" "Boutin"]
                                                                 [["Manuel Vacelet"] "4" "Manuel" "Vacelet"]]  )
    (fact "only retains non-emtpy names"
          (assemble-speakers [[:id :firstname :lastname :firstname :lastname :firstname :lastname] 
                              ["1" "" "" "Alexandre" "Boutin" "" ""]])   
          => [[:speakers :id :firstname :lastname :firstname :lastname :firstname :lastname]
              [["Alexandre Boutin"] "1"  "" "" "Alexandre" "Boutin"  "" "" ]]  )
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
      (second (sessions-as-maps sessions)) => (contains {:title "Challenge Kanban"}))
    
)


(defn keep-retained [parsed-csv]
  (filter (comp not-empty :retained) (sessions-as-maps parsed-csv)))

(fact "retains only sessions marked as such"
      (keep-retained ..csv..) => [{:title "happy scrumming" :retained "x"}]
      (provided (sessions-as-maps ..csv..) => [{:title "happy XP" :retained ""}
                                              {:title "happy scrumming" :retained "x"}]))
(defn add-non-session-data [[s1 s2 s3 s4 s5]]
  [{"all" {:title "Accueil des participants autour d'un café"}}
   {"all" {:title "Session Plénière: le mot des organisateurs & Sogilis"}}
   {"all" {:title "Keynote : Reinventing software quality" :speakers ["Gojko Adciz"]}}
   s1
   s2
   {"all" {:title "Repas"}} 
   {"all" {:title "Session Plénière: le mot des organisateurs & Samse"}}
   {"all" {:title "Keynote : Rompez les amarres !!" :speakers ["Laurent Sarrazin"]}}
   s3
   s4
   s5
   {"all" {:title "Apéro offert par le CARA"}}])

(fact "adds keynotes, coffe breaks, lunch to slots"
      (add-non-session-data [..s1.. ..s2.. ..s3.. ..s4.. ..s5.. ]) => (has-prefix [[{:title "Accueil des participants autour d'un café"}]
                                                   [{:title "Session Plénière: le mot des organisateurs & Sogilis"}]
                                                   [{:title "Keynote : Reinventing software quality" :speakers ["Gojko Adciz"]}]])
      (add-non-session-data [..s1.. ..s2.. ..s3.. ..s4.. ..s5.. ]) 
      => (contains [..s2.. 
                    [{:title "Repas"}] 
                    [{:title "Session Plénière: le mot des organisateurs & Samse"}]
                    [{:title "Keynote : XYZ" :speakers ["Laurent Sarrazin"]}]
                    ..s3..])
      (add-non-session-data [..s1.. ..s2.. ..s3.. ..s4.. ..s5.. ])
      => (has-suffix [..s5..
                      [{:title "Apéro offert par le CARA"}]]))
