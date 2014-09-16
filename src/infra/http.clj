(ns infra.http  
  (:use midje.sweet
        [compojure.core :only (GET POST defroutes)]
        [users.users :only (users)])
  (:require [clojure.java.io :as io] 
            [cemerick.friend :as friend]
            [infra.upload :as u] 
            [infra.handlers :as h] 
            [compojure.route :as route]
            [compojure.handler :as handler]
            (ring.middleware [multipart-params :as mp])
            [cemerick.friend.workflows :as workflows]
            [cemerick.friend.credentials :as creds]
            [ring.adapter.jetty :as jetty]))

(alter-var-root #'midje.semi-sweet/*include-midje-checks* (constantly false))

(def page-bodies {"/login" (u/login)})

(defroutes main-routes
  (GET "/" [] (u/render (u/index)))
  (GET "/askdfjasfasklfhasncvjkjfefdkfjksjfslkdjfnrefnedksfjhvn" [] (u/render (u/index)))
  (GET "/program" [] (u/render (u/sample)))
  (GET "/login" request (page-bodies (:uri request)))
  (GET ["/json/program-summary-with-roomlist"] request  (h/h-program-summary-with-roomlist))
  (GET ["/jsonp/program-summary-with-roomlist"] [callback]  (h/wrap-with-jsonp (h/h-program-summary-with-roomlist) callback))
  (GET ["/jsonp/beta/program-summary-with-roomlist"] [callback]  (h/h-beta-program-summary-with-roomlist callback))
  (GET ["/json/personas"] request  (h/h-personas))
  (GET ["/jsonp/slot-list"] [callback] (h/h-slot-list callback))
  (GET ["/jsonp/session/:id", :id #"[0-9]+"] 
       [callback id] (h/h-get-session id callback))
  (GET ["/jsonp/beta/session/:id", :id #"[0-9]+"] 
       [callback id] (h/h-beta-get-session (read-string id) callback))
  (GET ["/jsonp/get-slot/:slot", :slot #"[0-9]+"]
       [callback slot] (h/h-get-slot slot callback))
  (GET "/jsonp/current-sessions" [callback] (h/h-current-slot callback))
  (GET "/jsonp/upcoming-sessions" [callback] (h/h-upcoming-slot callback))
  (GET "/upload" [] (friend/authorize #{:admin} (u/upload)))
  (mp/wrap-multipart-params 
     (POST "/upload/sessions-csv" {params :params} 
           (friend/authorize #{:admin} (u/upload-file (params :file)))))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> main-routes
    (friend/authenticate {:credential-fn (partial creds/bcrypt-credential-fn users)
                          :workflows [(workflows/interactive-form)]})
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
  
  (facts "current-sessions provides an id, start-time and list of slots"
         (app {:uri "/jsonp/current-sessions" :request-method :get})
         => (contains {:body (contains "\"id\":")}))
  
  (defn -main [port]
    (jetty/run-jetty app {:port (Integer. port) :join? false}))
  
