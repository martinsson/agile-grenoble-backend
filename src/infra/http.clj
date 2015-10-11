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
  (GET "/login" request (page-bodies (:uri request)))
  (GET ["/jsonp/beta/program-summary-with-roomlist"] [callback]  (h/h-beta-program-summary-with-roomlist callback))
  (GET ["/jsonp/beta/session/:id", :id #"[0-9]+"] 
       [callback id] (h/h-beta-get-session (read-string id) callback))
  (GET "/upload" [] (friend/authorize #{:admin} (u/upload)))
  
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> main-routes
    (friend/authenticate {:credential-fn (partial creds/bcrypt-credential-fn users)
                          :workflows [(workflows/interactive-form)]})
    (handler/site)))

  (facts "provides a slot-list, sessions for a given slot and details of a session"
        (app {:uri "/jsonp/beta/program-summary-with-roomlist" :request-method :get}) 
        => (contains {:status 200 :body not-empty})
        (app {:uri "/jsonp/beta/session/3" :request-method :get}) 
        => (contains {:status 200 :body not-empty}))
  
  (facts "uses the callback query parameter to wrap the json"
        (app {:query-string "callback=myMethod" :uri "/jsonp/beta/program-summary-with-roomlist" :request-method :get}) 
        => (contains {:body (has-prefix "myMethod")})
        (app {:query-string "callback=someOtherMethod" :uri "/jsonp/beta/session/2" :request-method :get}) 
        => (contains {:body (has-prefix "someOtherMethod")}))

  (facts "provides a tuple of rooms and slots"
         (app {:uri "/json/program-summary-with-roomlist" :request-method :get})
         => (contains {:body (contains "Auditorium" )}))

  (defn -main [port]
    (jetty/run-jetty app {:port (Integer. port) :join? false}))
  
