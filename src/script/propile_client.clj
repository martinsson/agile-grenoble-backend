(ns script.propile-client
  (:use midje.sweet)
  (:require [clj-http.client :as client])
  (:require [script.clean-sessions :as clean-sessions]))
(defn get-propile-program [] (client/get "http://cfp.agile-grenoble.org/programs/42/full_export" {:as :json}))

(def propile-response-ref (ref (get-propile-program)))

; Update the program every 2 minutes

(future (let [two-minutes 120000] 
          (loop [] 
              (java.lang.Thread/sleep two-minutes)
              (dosync (ref-set propile-response-ref (get-propile-program)))
              (recur))))

(defn result [] (->> @propile-response-ref 
                 :body 
                 :program_entries 
                 (filter :session)))
(comment depart (:span_entire_row :session :comment :program_id :updated_at :session_id :created_at :track :id :slot)
         (:intended_audience :first_presenter_id :duration :session_type :materials_needed :topic 
          :state :laptops_required :updated_at :material_description :first_presenter :title :created_at 
          :other_limitations :material_url :experience_level :session_goal :short_description 
          :outline_or_timetable :max_participants :id :description :second_presenter_id :sub_title :room_setup)
)
(comment but (  :slides  :length :room  :email :firstname  :lastname))

(def propile-room-def 
  ["Auditorium" "Makalu" "Kili 1+2" "Kili 3+4" "Cervin" "Everest" "Mt-Blanc 1" "Mt-Blanc 2" "Mt-Blanc 3" "Mt-Blanc 4" "Atrium 1" "Atrium 2"])

(def room-count (count propile-room-def))

(defn width [session] (if (:span_entire_row session) room-count 1))


(defn room-name [session] (propile-room-def (dec (:track session))))

(defn speakers [{session :session}]
  (remove nil? [(get-in session [:first_presenter :name]) (get-in session [:second_presenter :name])]))
(defn speakers-details [{session :session}]
  (select-keys session [:first_presenter :second_presenter]))

(defn backend-session [raw-session]
  (let [tmp-session (update-in raw-session [:session :first_presenter] clean-sessions/filter-names-that-are-emails)
        session (update-in tmp-session [:session :second_presenter] clean-sessions/filter-names-that-are-emails)] 
    {:width (width session)
      :id (:id session)
      :room (room-name session)
      :title (get-in session [:session :title])
      :sub_title (get-in session [:session :sub_title]) ;??
      :abstract (get-in session [:session :short_description])
      :description (get-in session [:session :description])
      :benefits (get-in session [:session :session_goal]) ;??
      :theme (get-in session [:session :topic])
      :speakers (speakers session)
      :speakers-detail (speakers-details session)
      :type "session"
      :slot (:slot session)
      :length 3}))

(clojure.pprint/pprint (backend-session (nth (result) 1)))

(defn sessions [] 
  (for [s (result)] (backend-session s)))

(defn session [id] 
  (some #(when (= id (:id %)) %) (sessions)))
;todo speaker.name
