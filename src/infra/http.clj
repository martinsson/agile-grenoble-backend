(ns infra.http  
  (:use midje.sweet
        [compojure.core :only (GET POST defroutes)])
  (:require [infra.upload :as u] 
            [infra.handlers :as h] 
            [compojure.route :as route]
            [compojure.handler :as handler]
            (ring.middleware [multipart-params :as mp])))

(defroutes main-routes
  (GET "/" [] (u/render (u/index)))
  (GET "/inclusion" [] (u/render (u/inclusion)))
  (GET ["/json/program-summary-with-roomlist"] whatever (h/h-program-summary-with-roomlist h/local-file-loader whatever))
  (GET ["/jsonp/slot-list"] [callback] (h/h-slot-list callback))
  (GET ["/jsonp/session/:id", :id #"[0-9]+"] 
       [callback id] (h/h-get-session id callback))
  (GET ["/jsonp/sessions-for-slot/:slot", :slot #"[0-9]+"]
       [callback slot] (h/h-sessions-for slot callback))
  (GET "/jsonp/current-sessions" [callback] (h/h-sessions-for "3" callback))
  (GET "/jsonp/upcoming-sessions" [callback] (h/h-sessions-for "4" callback))
  (mp/wrap-multipart-params 
     (POST "/upload/sessions-csv" {params :params} (u/upload-file (params :file))))
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

  (facts "provides a tuple of rooms and slots"
         (app {:uri "/json/program-summary-with-roomlist" :request-method :get})
         => (contains {:body (contains "Auditorium" )}))