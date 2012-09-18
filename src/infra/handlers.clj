(ns infra.handlers  
  (:use midje.sweet)
  (:require [core.program-import :as pi]
            [core.sessions-api :as sa]
            [core.adding-missing-data :as amd]
            [clj-json.core :as json]))

(def local-file (clojure.java.io/resource "public/uploaded-sessions.csv"))
(defn decorate-sessions [] (amd/decorate-sessions local-file))

(def session-maps (pi/sessions-as-maps (amd/decorate-sessions local-file)))
(def sessions-for (partial sa/sessions-for session-maps))
(def get-session (partial sa/get-session session-maps))

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

(def h-session-list 
  (-> (partial response-map (decorate-sessions))
     (json-encode)
     (wrap-with-content-type-json)))

(defn h-sessions-for [slot callback] 
  (-> (partial response-map (sessions-for  slot))
     (json-encode)
     (wrap-with-jsonp callback)))

(defn h-slot-list [callback] 
  (-> (partial response-map (sa/slot-list))
     (json-encode)
     (wrap-with-jsonp callback)))

(defn h-get-session [session-id callback] 
  (-> (partial response-map (get-session session-id))
     (json-encode)
     (wrap-with-jsonp callback)))
