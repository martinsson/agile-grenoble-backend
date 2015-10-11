(ns infra.http  
  (:use midje.sweet
        [compojure.core :only (GET POST defroutes)]
         [net.cgrand.enlive-html :only [deftemplate]])
  (:require [clojure.java.io :as io] 
            [infra.handlers :as h] 
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.adapter.jetty :as jetty]))

(alter-var-root #'midje.semi-sweet/*include-midje-checks* (constantly false))

  
(defn render [t]
      (apply str t))

(deftemplate index "templates/index.html" [])


(defroutes main-routes
  (GET "/" [] (render (index)))
  (GET ["/jsonp/beta/program-summary-with-roomlist"] [callback]  (h/h-beta-program-summary-with-roomlist callback))
  (GET ["/jsonp/beta/session/:id", :id #"[0-9]+"] 
       [callback id] (h/h-beta-get-session (read-string id) callback))
  
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> main-routes
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
  
