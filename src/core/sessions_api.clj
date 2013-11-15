(ns core.sessions-api
  (:use midje.sweet)
  (:require [core.program-import :as pi]))

;TODO rename to butterfly api?

(def time-slots ["10:00" "10:15" "10:30" "10:45" "11:05" "11:20" "11:35" "11:50" "13:20" 
                 "13:45" "14:30"  "14:50" "15:05" "15:20" "15:35" "16:05" "16:20" "16:35" 
                 "16:50" "17:10" "17:25" "17:40" "17:55" "18:15"])
(def slot-list-basic (zipmap (iterate inc 1) time-slots))

(def personas {
    "eric" {
          "intitule" "Eric, Explorateur Agile", 
          "photo" "p7.png"
          },
    "mathieu"  {
            "intitule"  "Mathieu, Manager Produit",
            "photo" "p11.png"
            },
    "dimitri"  {
            "intitule"  "Dimitri, Développeur Agile",
            "photo" "devagile.png"
            },
    "patrick"  {
            "intitule"  "Patrick, Programmeur",
            "photo" "p4.png"
            },
    "alain"  {
            "intitule"  "Alain, Architecte Logiciel",
            "photo" "p10.png"
            },
    "tiana"  {
            "intitule"  "Tiana, Testeur/QA",
            "photo" "p3.png"
            },
	"carole"  {
            "intitule"  "Carole, Chef de Projet",
            "photo" "p2.png"
            },
	"stephane"  {
            "intitule"  "Stéphane, Scrum Master",
            "photo" "scm.png"
            },
	"philippe"  {
            "intitule"  "Philippe, Program Manager",
            "photo" "p6.png"
            },
	"claude"  {
            "intitule"  "Claude, Champion (ou coach interne)",
            "photo" "p1.png"
            },
	"christophe"  {
            "intitule"  "Christophe, Consultant",
            "photo" "p13.png"
            },
	"adrien"  {
            "intitule"  "Adrien, Analyste Métier/Fonctionnel",
            "photo" "p12.png"
            },
	"daphne"  {
            "intitule"  "Daphné, Designer UI/Ergonome",
            "photo" "p9.png"
            },
	"denis"  {
            "intitule"  "Denis, Dirigeant d'entreprise (ou Manager R&D)", 
            "photo" "dirigeant.png"
            }})
(defn persona-list []
  (into {} (for [[k v] personas] {k (update-in v ["photo"] (partial str "http://2013.agile-grenoble.org/personas/"))})))
  (fact
    (get-in (persona-list) ["eric" "photo"] ) => "http://2013.agile-grenoble.org/personas/p7.png" )

(defn slot-list []
  (list slot-list-basic))
  (facts
    (slot-list) => [{1 "10:00", 2 "10:15", 3 "10:30", 4 "10:45", 5 "11:05", 6 "11:20", 
                     7 "11:35", 8 "11:50", 9 "13:20", 10 "13:45", 11 "14:30", 12 "14:50", 
                     13 "15:05", 14 "15:20", 15 "15:35", 16 "16:05", 17 "16:20", 18 "16:35", 
                     19 "16:50", 20 "17:10", 21 "17:25", 22 "17:40", 23 "17:55", 24 "18:15"}])

(defn session-list-for 
  ([session-maps slot]
  (let [reduce-keys #(select-keys % [:id :title :slot :room :speakers :type :length :theme :width])
        found-sessions (filter #(= slot (% :slot)) session-maps)] 
    (for [s (map reduce-keys found-sessions)]
              (assoc-in s [:start-time] (slot-list-basic (Integer/valueOf (:slot s)))) ))))

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
     => (just [{:id ..id.. :title ..title.. :slot "3" :room ..room.. :speakers ..sl.. :start-time "10:30"}])
     :in-any-order)
  
(defn get-slot [session-maps slot]
  {:id slot 
   :start-time (slot-list-basic (Integer/valueOf slot)) 
   :sessions (session-list-for session-maps slot)})

  (facts "current-sessions provides an id, start-time and list of slots"  
    (get-slot ..maps.. "2") => (contains {:id "2" :start-time "10:15"})
    (get-slot ..maps.. "5") => (contains {:sessions ..sessions..})
    (provided (session-list-for ..maps.. "5") => ..sessions..))

(defn- get-session-simple 
  ([session-maps id]
  (first (filter #(= id (% :id)) session-maps))))

;; remove this wrapper when we handle the 120min sessions better
(defn get-session 
  ([session-maps id]
  (let [session (get-session-simple session-maps id)
        second-id  (:2nd-id session)] 
    (if (empty? second-id)
      session
      (get-session-simple session-maps second-id) ;it is a 2nd part session, get the real one
      ))))


  (facts "it gets a session based on its id"
    (get-session [{:id ..id.. :title "Hej"}] ..id..) => {:id ..id.. :title "Hej"}
    (get-session [{:id ..id1.. :title "Hej"}
                  {:id ..id2.. :title "Hopp"}
                  {:id ..id3.. :title "Daa"}] ..id2..) => {:id ..id2.. :title "Hopp"})
  
  (fact "it handles the 2nd part sessions"
    (get-session [{:id "22" :title "le must"}
                  {:id "99" :2nd-id "22"}] "99") => {:id "22" :title "le must"}
    (get-session [{:id "22" :title "le must"}
                  {:id "99" :2nd-id "" :title "CI for dummies"}] "99") => (contains {:id "99" :title "CI for dummies"}))

