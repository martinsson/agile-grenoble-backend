(ns infra.handlers  
  (:use midje.sweet)
  (:require [core.program-import :as pi]
            [core.sessions-api :as sa]
            [core.current-sessions :as cs]
            [script.propile-client :as pc] 
            [clj-json.core :as json]
            [clojure.java.jdbc :as jdbc]))

(def local-file (clojure.java.io/file (str (or (System/getProperty "app.basedir") (System/getProperty "user.home")) "/uploaded-sessions.csv")))

(defn decorate-sessions 
  ([csv-resource] 
  (pi/append-speaker-maps (pi/normalized-sessions csv-resource))))

(defn session-maps-file [f] (map (comp pi/add-speaker-fullnames pi/make-list-of-personas) (pi/keep-retained (decorate-sessions f))))

(def db-spec (or (System/getenv "DATABASE_URL")
              "postgresql://localhost:5432/sessions"))

(defn speakers-to-list [m] (update-in m [:speakers] list))
(defn slots-to-int [m] (update-in m [:slot] read-string))
(defn smaps-pg [] (jdbc/query db-spec ["select * from sessions"] :row-fn (comp slots-to-int speakers-to-list)))

(def smaps (ref (session-maps-file local-file)))
(defn session-list-for [slot] (sa/session-list-for @smaps slot))
(defn get-session [id] (sa/get-session @smaps id))

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
               "Mt-Blanc 4" {:id 9, :capacity 24}})

(defn all-slots-with-rooms 
  ([] (all-slots-with-rooms (smaps-pg)))
  ([smaps] (let [;header   (keys (first @smaps))
          index-by-room #(zipmap (map :room %) %)
          slots    (for [slot (range 1 30)] (sa/session-list-for smaps slot))
          all-slots (pi/add-non-session-data (map index-by-room slots))]
      {:rooms room-defs
       :slots (remove empty? all-slots)
       :sessions smaps
       })))
  (facts "returns a roomlist"
         (:rooms (all-slots-with-rooms)) =>
         (contains ["Auditorium" "Kili 1+2" "Kili 3+4" "Mt-Blanc 1" "Mt-Blanc 2" "Mt-Blanc 3" "Mt-Blanc 4" "Everest" "Cervin" "Makalu"] :in-any-order))
  (facts "returns a list of slots, indexed by room"
         (all-slots-with-rooms) =>
         (contains {:slots (contains (contains {"Auditorium" not-empty
                                                "Mt-Blanc 3" not-empty}))}))
  (facts "there are 10 rooms"
         (count (:rooms (all-slots-with-rooms))) =>
         10) 
  

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
(defn h-current-slot [callback]
  (h-get-slot (str (cs/current-slot-id)) callback))

(defn h-upcoming-slot [callback]
  (h-get-slot (str (cs/upcoming-slot-id)) callback))


(defn h-slot-list [callback] 
  (-> (partial response-map (sa/slot-list))
     (json-encode)
     (wrap-with-jsonp callback)))


(defn h-get-session [session-id callback] 
  (-> (partial response-map (sa/get-session @smaps session-id))
     (json-encode)
     (wrap-with-jsonp callback)))

(defn h-beta-get-session [session-id callback] 
  (-> (partial response-map (pc/session session-id))
     (json-encode)
     (wrap-with-jsonp callback)))

(defn h-program-summary-with-roomlist []  
  (-> (partial response-map (all-slots-with-rooms (smaps-pg)))
     (json-encode)
     (wrap-with-content-type-json)))

(defn h-beta-program-summary-with-roomlist [callback]  
  (-> (partial response-map (all-slots-with-rooms (pc/sessions)))
     (json-encode)
     (wrap-with-jsonp callback)))

(defn h-personas [] 
  (-> (partial response-map (sa/persona-list))
     (json-encode)
     (wrap-with-content-type-json)))
