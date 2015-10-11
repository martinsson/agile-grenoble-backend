(ns infra.handlers  
  (:use midje.sweet)
  (:require [core.sessions-api :as sa]
            [script.propile-client :as pc] 
            [clj-json.core :as json]
            [clojure.java.jdbc :as jdbc]))

(def room-defs {
               "Auditorium" {:id 0, :capacity 530}
               "Makalu"     {:id 1, :capacity 110} 
               "Kili 1+2"   {:id 2, :capacity 55}
               "Kili 3+4"   {:id 3, :capacity 55}
               "Cervin"     {:id 4, :capacity 40}
               "Everest"    {:id 5, :capacity 40}
               "Mt-Blanc 1" {:id 6, :capacity 24}
               "Mt-Blanc 2" {:id 7, :capacity 24}
               "Mt-Blanc 3" {:id 8, :capacity 24}
               "Mt-Blanc 4" {:id 9, :capacity 24}
               "Atrium"     {:id 10 :capacity 100}})


(defn add-non-session-data [[s1 s2 s3 s4 s5 s6 s7 s8]]
  (let [non-sessions  [{:title "Accueil des participants autour d'un café" :type :non-session}
                       {:title "Session Plénière: le mot des organisateurs & Sponsor" :type :sponsor}
                       {:title "Changement de salle" :type :non-session}
                       {:title "Pause café" :type :cafe}
                       {:title "Pause déjeuner" :type :meal} 
                       {:title "Session Plénière: le mot des organisateurs & Sponsor" :type :sponsor}
                       {:title "Changement de salle" :type :non-session}
                       {:title "Pause café" :type :cafe}
                       {:title "Pause café" :type :cafe}
                       {:title "Changement de salle" :type :non-session}
                       
                       ;{:title "Fin de journée" :type :departure}
                       ]
        [arr sp1 chgmt1 cafe1 meal sp2 chgmt2 cafe2 cafe3 chgmt3 chgmt4] (for [ns non-sessions] {"all" (assoc ns :length 1 :width 11)})]
    [arr sp1 s1 chgmt1 s2 cafe1 s3 meal sp2 s4 chgmt2 s5 cafe2 s6 cafe3 s7 chgmt3 s8]))



(defn all-slots-with-rooms 
  
  ([smaps] (let [;header   (keys (first @smaps))
          index-by-room #(zipmap (map :room %) %)
          slots    (for [slot (range 1 15)] (sa/session-list-for smaps slot))
          all-slots (add-non-session-data (map index-by-room slots))]
      {:rooms room-defs
       :slots (remove empty? all-slots)
       ;:sessions smaps  // to speed up the UI dont need this for the program summary. 
       })))
  (facts "returns a roomlist"
         (keys (:rooms (all-slots-with-rooms {}))) =>
         (contains ["Auditorium" "Kili 1+2" "Kili 3+4" "Mt-Blanc 1" "Mt-Blanc 2" "Mt-Blanc 3" "Mt-Blanc 4" "Everest" "Cervin" "Makalu" "Atrium"] :in-any-order))
  (future-facts "returns a list of slots, indexed by room"
         (all-slots-with-rooms {}) =>
         (contains {:slots (contains (contains {"Auditorium" not-empty
                                                "Mt-Blanc 3" not-empty}))}))
  (facts "there are 11 rooms"
         (count (:rooms (all-slots-with-rooms {}))) =>
         11) 
  

  (future-facts "adapt to all-slots-with-rooms : returns a list of slots with a list of sessions"
         (first (nth (all-slots-with-rooms) 3)) => (contains {:slot "1", :title "DevOps@Kelkoo", :id "10"} :in-any-order)
         (count (all-slots-with-rooms)) => 14)

(defn response-map [arg request]
  {:status 200 :body arg})

    (fact "wraps in a response-map"
      (response-map "toto" nil ) => {:status 200 :body "toto"}
      (response-map 145 nil)    => {:status 200 :body 145})

(defn wrap-with-content-type-json [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Content-Type"] "application/json; charset=UTF-8")))) 

    (facts 
      ((wrap-with-content-type-json ..handler..) ..request..) => {:headers {"Content-Type" "application/json; charset=UTF-8"}}
      (provided (..handler.. ..request..) => {}))

(defn wrap-with-jsonp [handler function-name]
  (wrap-with-content-type-json 
    (fn [request]
      (let [response (handler request)
            wrap-with-function #(str function-name "(" % ")")]
        (update-in response [:body] wrap-with-function)))))

    (facts 
      (against-background (..handler.. ..request..) => {:body ..some-json..})
      ((wrap-with-jsonp ..handler.. "getAllTimeSlots") ..request..) 
          => (contains {:body (str "getAllTimeSlots(" ..some-json.. ")")})
      ((wrap-with-jsonp ..handler.. "getSessionsForSlot") ..request..) 
          => (contains {:body (str "getSessionsForSlot(" ..some-json.. ")")}))

(defn json-encode [handler]
  (fn [request]
    (let [response (handler request)]
      (update-in response [:body] json/generate-string))))

(defn h-beta-get-session [session-id callback] 
  (-> (partial response-map (pc/session session-id))
     (json-encode)
     (wrap-with-jsonp callback)))

(defn h-beta-program-summary-with-roomlist [callback]  
  (-> (partial response-map (all-slots-with-rooms (pc/sessions)))
     (json-encode)
     (wrap-with-jsonp callback)))
