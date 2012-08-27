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

(def session-list 
  (-> amd/sessions-with-missing-data
     (json-encode)
     (wrap-with-content-type-json)))

(defn response-map [arg request]
  {:status 200 :body arg})

(defn sessions-for [slot callback] 
  (-> (partial response-map (sa/sessions-for slot))
     (json-encode)
     (wrap-with-jsonp callback)))

(defn slot-list [callback] 
  (-> (partial response-map (sa/slot-list))
     (json-encode)
     (wrap-with-jsonp callback)))

(defn get-session [session-id callback] 
  (-> (partial response-map (sa/get-session session-id))
     (json-encode)
     (wrap-with-jsonp callback)))

(defroutes main-routes
  (GET "/" [] "<h1>Bonjour Agile Grenoble !</h1>")
  (GET "/session-list" request (session-list request))
  (GET ["/jsonp/slot-list"] [callback] (slot-list callback))
  (GET ["/jsonp/session/:id", :id #"[0-9]+"] [id callback] (get-session id callback))
  (GET ["/jsonp/sessions-for-slot/:slot", :slot #"[0-9]+"]
       [callback slot] (sessions-for (Integer/parseInt slot) callback))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> main-routes
    (handler/site)))

;;; experimental
(defn session-list2 [request]
  "doesn work"
  (let [sessions (response-map request amd/decorated-sessions)] 
    (-> sessions
     (json-encode)
     (wrap-with-content-type-json))))
(fact "wraps in a response-map"
  (response-map "toto" nil ) => {:status 200 :body "toto"}
  (response-map 145 nil)    => {:status 200 :body 145})


