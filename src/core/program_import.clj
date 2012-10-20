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
                     "room" :room
                     "Bio | Biography" :bio
                     "2nd part of" :2nd-id})

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

(defn indexes-of [col e]
  ;returns all indexes of e in col
  (keep-indexed #(if (= e %2) %1) col))
(def speaker-keys [:firstname :lastname :bio])
(defn- speaker-map [keys pos-matrix line]
  (let [vline (vec line)
        value-map (for [s pos-matrix] (map vline s))]  
    (map #(zipmap keys  %)  value-map)))

(defn extract-speakers [csv keys]
  (let [body           (rest csv)
        header         (first csv)
        pos-matrix     (apply map list (for [k keys] (indexes-of header k)))]
    (map (partial speaker-map keys pos-matrix) body)))

  (fact "it returns a list of map where the values are the successive positions of the given keys "
        (extract-speakers [[:firstname :bio :firstname :bio]
              ["johan" "de suede" "bernard" "des states"]
              ["katia" "informaticienne" "aline" "graphiste"]] [:firstname :bio]) => [[{:bio "de suede" :firstname "johan"}
                                                                  {:bio "des states" :firstname "bernard"}]
                                                                  [{:bio "informaticienne" :firstname "katia"}
                                                                   {:bio "graphiste" :firstname "aline"}]])
  
(defn append-speaker-maps [normalized-csv]
  (let [body           (rest normalized-csv)
        header         (first normalized-csv)
        pos-matrix     (apply map list (for [k speaker-keys] (indexes-of header k)))]
    (cons (cons :speaker-list header)
          (map #(cons (speaker-map speaker-keys pos-matrix %) %) body))))
  

(defn add-speaker-fullnames [session] 
  (let [fullname (fn [{:keys [firstname lastname]}] (clojure.string/join " " [firstname lastname]))
        filter-blank (partial filter (complement clojure.string/blank?))]
    (assoc-in session [:speakers] 
               (filter-blank (map fullname (:speaker-list session))))))
          
  (fact "appends element :speakers that contains the fullname of all speakers"
    (add-speaker-fullnames {:title "whatever" :speaker-list [{:firstname "jean-claude" :lastname "dusse"}]})
    => {:title "whatever" 
        :speaker-list [{:firstname "jean-claude" :lastname "dusse"}]
        :speakers ["jean-claude dusse"]})

(defn sessions-as-maps [parsed-csv]
  (let [header (first parsed-csv)
        body   (rest parsed-csv)
        index-with-header (partial zipmap header)
        add-type          #(assoc-in % [:type] :session)]
    (map (comp add-type index-with-header) body)))

  ;TODO mock data
  (let [sessions (normalized-sessions local-file)] 
    (facts "transforms the cleaned csv to a list of maps, keys being the csv columns"
      (sessions-as-maps sessions) => (has every? #(% :title))
      (first (sessions-as-maps sessions)) => (contains {:title "Approche pragmatique pour industrialiser le développement d’applications"})
      (second (sessions-as-maps sessions)) => (contains {:title "Challenge Kanban"}))
    (fact "it has a key :type with value :session"
          (sessions-as-maps sessions) => (has every? #(= :session (% :type)))))

(defn keep-retained [parsed-csv]
  (filter (comp not-empty :retained) (sessions-as-maps parsed-csv)))

  (fact "retains only sessions marked as such"
      (keep-retained ..csv..) => [{:title "happy scrumming" :retained "x"}]
      (provided (sessions-as-maps ..csv..) => [{:title "happy XP" :retained ""}
                                              {:title "happy scrumming" :retained "x"}]))

(defn add-non-session-data [[s1 s2 s3 s4 s5]]
  (let [non-sessions  [{:title "Accueil des participants autour d'un café" :type :arrival}
                       {:title "Session Plénière: le mot des organisateurs & Sogilis" :type :sponsor}
                       {:title "Keynote : Reinventing software quality" :speakers ["Gojko Adzic"] :type :keynote}
                       {:title "Repas" :type :meal} 
                       {:title "Session Plénière: le mot des organisateurs & Samse" :type :sponsor}
                       {:title "Keynote : Rompez les amarres !!" :speakers ["Laurent Sarrazin"] :type :keynote}
                       {:title "Pause café" :type :cafe}
                       {:title "Apéro offert par le Club Agile Rhone Alpes" :type :apero}
                       {:title "Fin de journée" :type :departure}]
        [arr sp1 kn1 meal sp2 kn2 cafe apero dep] (for [ns non-sessions] {"all" ns})]
    [arr sp1 kn1 s1 s2 meal sp2 kn2 s3 cafe s4 s5 apero dep]))

(future-fact "adds keynotes, coffe breaks, lunch to slots"
      (add-non-session-data [..s1.. ..s2.. ..s3.. ..s4.. ..s5.. ]) => (has-prefix [{"all" (contains {:title "Accueil des participants autour d'un café"})}
                                                   {"all" (contains {:title "Session Plénière: le mot des organisateurs & Sogilis"})}
                                                   {"all" (contains {:title "Keynote : Reinventing software quality" :speakers ["Gojko Adzic"]})}])
      (add-non-session-data [..s1.. ..s2.. ..s3.. ..s4.. ..s5.. ]) 
      => (contains [..s2.. 
                    {"all" (contains {:title "Repas"})} 
                    {"all" (contains {:title "Session Plénière: le mot des organisateurs & Samse"})}
                    {"all" (contains {:title "Keynote : Rompez les amarres !!" :speakers ["Laurent Sarrazin"]})}
                    ..s3..])
      (add-non-session-data [..s1.. ..s2.. ..s3.. ..s4.. ..s5.. ])
      => (has-suffix [{"all" {:title "Pause café"}}
                      ..s4..
                      ..s5..
                      {"all" {:title "Apéro offert par le Club Agile Rhone Alpes"}}]))
(future-facts "adds a type to every slot"
       (add-non-session-data [..s1.. ..s2.. ..s3.. ..s4.. ..s5.. ]) =>
       (has-prefix [{"all" {:type :arrival :title "Accueil des participants autour d'un café" }}]))
