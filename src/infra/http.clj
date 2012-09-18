(ns infra.http  
  (:use compojure.core midje.sweet infra.upload)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [core.program-import :as pi]
            [core.sessions-api :as sa]
            [core.adding-missing-data :as amd]
            [clj-json.core :as json]
            (ring.middleware [multipart-params :as mp])))

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

(defroutes main-routes
  (GET "/" [] (render (index)))
  (GET "/session-list" request (h-session-list request))
  (GET ["/jsonp/slot-list"] [callback] (h-slot-list callback))
  (GET ["/jsonp/session/:id", :id #"[0-9]+"] 
       [callback id] (h-get-session id callback))
  (GET ["/jsonp/sessions-for-slot/:slot", :slot #"[0-9]+"]
       [callback slot] (h-sessions-for slot callback))
  (mp/wrap-multipart-params 
     (POST "/upload/sessions-csv" {params :params} (upload-file (params :file))))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> main-routes
    (handler/site)))

  (facts "provides a slot-list, sessions for a given slot and details of a session"
        (app {:uri "/jsonp/slot-list" :request-method :get}) 
        => (contains {:status 200 :body not-empty})
        (app {:uri "/jsonp/sessions-for-slot/3" :request-method :get}) 
        => (contains {:status 200 :body not-empty})
        (app {:uri "/jsonp/session/7" :request-method :get}) 
        => (contains {:status 200 :body not-empty}))
  
  (facts "uses the callback query parameter to wrap the json"
        (app {:query-string "callback=myMethod" :uri "/jsonp/slot-list" :request-method :get}) 
        => (contains {:body (has-prefix "myMethod")})
        (app {:query-string "callback=someOtherMethod" :uri "/jsonp/sessions-for-slot/2" :request-method :get}) 
        => (contains {:body (has-prefix "someOtherMethod")})
        (app {:query-string "callback=aThirdMethod" :uri "/jsonp/session/17" :request-method :get}) 
        => (contains {:body (has-prefix "aThirdMethod")}))

