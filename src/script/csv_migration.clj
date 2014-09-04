(ns script.csv-migration
  (:require [clojure.java.jdbc :as jdbc]
            [infra.handlers :as h]))

(defn extractor [session-map] (update-in (select-keys session-map ["slides" :width :bio :benefits :length :room :retained :title :speakers
                                                                   :firstname :lastname :theme :abstract "email" :id :slot]) [:speakers] first))

(defn -main [command & [url]]
  (let [db-spec (or url "postgresql://localhost:5432/sessions")
        csv-file (h/session-maps-file h/local-file)]
    (case command
      "drop" (jdbc/db-do-commands db-spec (jdbc/drop-table-ddl :sessions))
      "create"
        (jdbc/db-do-commands db-spec
          (jdbc/create-table-ddl :sessions
          ["slides" "varchar(255)"]
          [:width "varchar(32)"]
          [:bio  "text"]
          [:benefits  "text"]
          [:length  "varchar(32)"]
          [:room  "varchar(32)"]
          [:retained "varchar(32)"]
          [:title  "varchar(255)"]
          [:speakers  "varchar(255)"]
          [:firstname  "varchar(32)"]
          [:lastname  "varchar(32)"]
          [:theme  "varchar(32)"]
          [:abstract  "text"]
          ["email"  "varchar(32)"]
          [:id  "varchar(32)"]
          [:slot  "varchar(32)"]))
      "inject" (for [session-map (map extractor csv-file)] (jdbc/insert! db-spec :sessions session-map))
      "either of [drop, create or inject")))

(defn toto [route]
  (case route
    "home" "go home"
    "away" "go away"
    "stay"))
;(clojure.pprint/pprint line-one)

;  [:speaker-list "varchar(32)"]
  ;[:personas  "varchar(32)"]
;  ["Téléphone | Phone"  "varchar(32)"]
;  ["Dispositions particulières de la salle, Remarques | Room requirements, Remarks."  "varchar(32)"]
;  ["Personas (optionnel, 3 max, 0 choix = tous publics)"  "varchar(32)"]
;  ["Confirmation (Date + Nom)"  "varchar(32)"]
;  ["Web site / Blog / Twitter..."  "varchar(32)"]
  ;["Horodateur"  "varchar(32)"]
;  ["Thème | Theme" "varchar(32)"]
;  ["Entreprise | Company"  "varchar(32)"]
  ;[:type  "varchar(32)"] 
;  ["Nombre de participants maxi | Maximum number of participants"  "varchar(32)"]
;  ["Durée | Duration"  "varchar(32)"]
;  ["Niveau | Level"  "varchar(32)"]
  ;  ["Mail refus"  "varchar(32)"]
;  ["Contacté pour plus d'info sur la session" :boolean]
