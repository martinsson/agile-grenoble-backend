(ns infra.http  
  (:use compojure.core midje.sweet)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [core.program-import :as pi]
            [core.sessions-api :as sa]
            [core.adding-missing-data :as amd]
            [clj-json.core :as json]))


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

(def core-handler
  (-> pi/get-session-3
     (json-encode)
     (wrap-with-content-type-json)))

(def session-list 
  (-> amd/sessions-with-missing-data
     (json-encode)
     (wrap-with-content-type-json)))

(defn slot-list-body [request] 
  {:status 200
   :body (sa/slot-list)})

(defn sessions-for-body [slot request] 
  {:status 200
   :body (sa/sessions-for slot)})

(def slot-list 
  (-> slot-list-body
     (json-encode)
     (wrap-with-jsonp "getAllTimeSlots")))

(defn sessions-for [slot] 
  (-> (partial sessions-for-body slot)
     (json-encode)
     (wrap-with-jsonp "getSessionsForSlot")))

(defroutes main-routes
  (GET "/" [] "<h1>Bonjour Agile Grenoble !</h1>")
  (GET "/json" request (core-handler request))
  (GET "/session-list" request (session-list request))
  (GET "/jsonp/slot-list" request (slot-list request))
  (GET ["/jsonp/sessions-for-slot/:slot", :slot #"[0-9]+"] [slot] (sessions-for (Integer/parseInt slot)))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> main-routes
    (handler/site)))

;;; experimental
(defn response-map [request arg]
  {:status 200 :body arg})
(defn session-list2 [request]
  "doesn work"
  (let [sessions (response-map request amd/decorated-sessions)] 
    (-> sessions
     (json-encode)
     (wrap-with-content-type-json))))
(fact "wraps in a response-map"
  (response-map nil "toto") => {:status 200 :body "toto"}
  (response-map nil 145)    => {:status 200 :body 145})


