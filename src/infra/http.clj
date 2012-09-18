(ns infra.http  
  (:use compojure.core midje.sweet infra.upload infra.handlers)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            (ring.middleware [multipart-params :as mp])))

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

