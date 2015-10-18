(ns infra.handlers  
  (:use midje.sweet)
  (:require [core.sessions-api :as sa]
            [script.propile-client :as pc] 
            [clj-json.core :as json]
            [clojure.java.jdbc :as jdbc]))


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
                       {:title "Changement de salle" :type :non-session}
                       {:title "Apéro offert par le CARA" :type :departure}
                       
                       ;{:title "Fin de journée" :type :departure}
                       ]
        [arr sp1 chgmt1 cafe1 meal sp2 chgmt2 cafe2 cafe3 chgmt3 chgmt4 apero] (for [ns non-sessions] {"all" (assoc ns :length 1 :width pc/room-count)})]
    [arr sp1 s1 chgmt1 s2 cafe1 s3 meal sp2 s4 chgmt2 s5 cafe2 s6 cafe3 s7 chgmt3 s8 apero]))



(defn all-slots-with-rooms 
  
  ([smaps] (let [more-than-number-of-slots 15  ;header   (keys (first @smaps))
          index-by-room #(zipmap (map :room %) %)
          slots    (for [slot (range 1 more-than-number-of-slots)] (sa/session-list-for smaps slot))
          all-slots (add-non-session-data (map index-by-room slots))]
      {:rooms pc/room-defs
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
  (facts "there are 12 rooms"
         (count (:rooms (all-slots-with-rooms {}))) =>
         12) 
  

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
