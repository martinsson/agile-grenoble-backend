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

(def room-defs 
  (array-map   "Auditorium" {:id 0, :capacity 530}
               "Makalu"     {:id 1, :capacity 110} 
               "Kili 1+2"   {:id 2, :capacity 55}
               "Kili 3+4"   {:id 3, :capacity 55}
               "Cervin"     {:id 4, :capacity 40}
               "Everest"    {:id 5, :capacity 40}
               "Mt-Blanc 1" {:id 6, :capacity 24}
               "Mt-Blanc 2" {:id 7, :capacity 24}
               "Mt-Blanc 3" {:id 8, :capacity 24}
               "Mt-Blanc 4" {:id 9, :capacity 24}
               "Atrium 1"   {:id 10 :capacity 20}
               "Atrium 2"   {:id 11 :capacity 30}))

(def room-count (count room-defs))

(def propile-room-def 
  (into [] (keys room-defs)))
(facts 
  propile-room-def => ["Auditorium" "Makalu" "Kili 1+2" "Kili 3+4" "Cervin" "Everest" "Mt-Blanc 1" "Mt-Blanc 2" "Mt-Blanc 3" "Mt-Blanc 4" "Atrium 1" "Atrium 2"])

(defn width [session] (if (:span_entire_row session) room-count 1))

(defn room-name [session] 
  (let [track-index (dec (:track session))] 
    (propile-room-def track-index)))

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
