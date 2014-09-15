(ns script.propile-client)
(defonce propile-response (client/get "http://cfp.agile-grenoble.org/programs/5/full_export" {:as :json}))

(def result (->> propile-response 
             :body 
             :program_entries 
             (filter :session)))
(comment depart (:span_entire_row :session :comment :program_id :updated_at :session_id :created_at :track :id :slot))
(comment but (:width  :slides :benefits :length :room :retained :title :speakers :email :firstname :type :lastname :theme :abstract :id :slot))

(defn width [session] (if (:span_entire_row session) 1 8))

(defn room-name [track-number] "makalu")
(defn backend-session [session]
  {:width (width session)
   :id (:id session)
   :room (room-name :track)
   :title (get-in session [:session :title])
   :speakers (remove nil? [(get-in session [:session :first_presenter :name]) (get-in session [:session :second_presenter :name])])})

(clojure.pprint/pprint (backend-session (nth result 5)))


;todo speaker.name
