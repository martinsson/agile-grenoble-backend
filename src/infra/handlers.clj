(ns infra.handlers  
  (:use midje.sweet)
  (:require [core.program-import :as pi]
            [core.sessions-api :as sa]
            [clj-json.core :as json]))

(def local-file (clojure.java.io/file (str (System/getProperty "user.home") "/uploaded-sessions.csv")))

(defn decorate-sessions 
  ([csv-resource] 
  (pi/append-speaker-maps (pi/normalized-sessions csv-resource))))

(defn session-maps [] (map pi/add-speaker-fullnames (pi/keep-retained (decorate-sessions local-file))))
(def smaps (ref (session-maps)))
(defn session-list-for [slot] (sa/session-list-for @smaps slot))
(defn get-session [id] (sa/get-session @smaps id))

(defn all-slots-with-rooms [csv-file] 
  (let [sessions (pi/normalized-sessions csv-file)
        header   (first sessions)
        body     (rest sessions)
        roomidx  (.indexOf (vec header) :room)
        index-by-room #(zipmap (map :room %) %)
        slots    (for [slot (range 1 6)] (sa/session-list-for @smaps (str slot)))]
    {:rooms (filter not-empty (set (map #(nth % roomidx) body)))
     :slots (pi/add-non-session-data (map index-by-room slots))
     :sessions (into {} (for [s @smaps] {(s :id) s}))}))


  (facts "returns a roomlist"
         (all-slots-with-rooms local-file) =>
         (contains {:rooms ["Auditorium" "Kilimanjaro 1" "Mont Blanc 1" "Kilimanjaro 3" "Mont Blanc 4" "Everest" "Cervin" "Mont Blanc 3+2" "Makalu"]}))
  (facts "returns a list of slots, indexed by room"
         (all-slots-with-rooms local-file) =>
         (contains {:slots (contains (contains {"Auditorium" not-empty
                                      "Mont Blanc 1" not-empty}))}))
  (facts "there are 9 rooms"
         (count (:rooms (all-slots-with-rooms local-file))) =>
         9) 
  (facts ":sessions is an indexed list of all sessions"
         (all-slots-with-rooms local-file) => 
         (contains {:sessions anything})
         (:sessions (all-slots-with-rooms local-file)) => 
         (contains {"3" anything}))

  (future-facts "adapt to all-slots-with-rooms : returns a list of slots with a list of sessions"
         (first (nth (all-slots-with-rooms local-file) 3)) => (contains {:slot "1", :title "DevOps@Kelkoo", :id "10"} :in-any-order)
         (count (all-slots-with-rooms local-file)) => 14)

(defn response-map [arg request]
  {:status 200 :body arg})

    (fact "wraps in a response-map"
      (response-map "toto" nil ) => {:status 200 :body "toto"}
      (response-map 145 nil)    => {:status 200 :body 145})

(defn wrap-with-content-type-json [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Content-Type"] "application/json"))))

    (facts 
      ((wrap-with-content-type-json ..handler..) ..request..) => {:headers {"Content-Type" "application/json"}}
      (provided (..handler.. ..request..) => {}))

(defn wrap-with-jsonp [handler function-name]
  (fn [request]
    (let [response (handler request)
          wrap-with-function #(str function-name "(" % ")")]
      (update-in response [:body] wrap-with-function))))

    (facts 
      (against-background (..handler.. ..request..) => {:body ..some-json..})
      ((wrap-with-jsonp ..handler.. "getAllTimeSlots") ..request..) 
          => {:body (str "getAllTimeSlots(" ..some-json.. ")")}
      ((wrap-with-jsonp ..handler.. "getSessionsForSlot") ..request..) 
          => {:body (str "getSessionsForSlot(" ..some-json.. ")")})

(defn json-encode [handler]
  (fn [request]
    (let [response (handler request)]
      (update-in response [:body] json/generate-string))))

(defn h-session-list-for [slot callback] 
  (-> (partial response-map (sa/session-list-for @smaps slot))
     (json-encode)
     (wrap-with-jsonp callback)))

(defn h-get-slot [slot callback] 
  (-> (partial response-map (sa/get-slot @smaps slot))
     (json-encode)
     (wrap-with-jsonp callback)))

(defn h-slot-list [callback] 
  (-> (partial response-map (sa/slot-list))
     (json-encode)
     (wrap-with-jsonp callback)))

(defn h-get-session [session-id callback] 
  (-> (partial response-map (sa/get-session @smaps session-id))
     (json-encode)
     (wrap-with-jsonp callback)))

(defn h-program-summary-with-roomlist []  
  (-> (partial response-map (all-slots-with-rooms local-file))
     (json-encode)
     (wrap-with-content-type-json)))
