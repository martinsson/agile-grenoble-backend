(ns script.propile-client)
(defonce propile-response (client/get "http://cfp.agile-grenoble.org/programs/6/full_export" {:as :json}))

(def result (->> propile-response 
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

(defn width [session] (if (:span_entire_row session) 1 8))

(defn room-name [track-number] "makalu")
(defn backend-session [session]
  {:width (width session)
   :id (:id session)
   :room (room-name :track)
   :title (get-in session [:session :title])
   :sub_title (get-in session [:session :sub_title]) ;??
   :abstract (get-in session [:session :short_description])
   :description (get-in session [:session :description])
   :benefits (get-in session [:session :session_goal]) ;??
   :theme (get-in session [:session :topic])
   :speakers (remove nil? [(get-in session [:session :first_presenter :name]) (get-in session [:session :second_presenter :name])])
   :type (get-in session [:session :session_type ])
   :slot (:slot session)
   :length 3
   })

(clojure.pprint/pprint (backend-session (nth result 3)))


;todo speaker.name
