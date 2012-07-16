(ns infra.http  
  (:use compojure.core)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [core.program-import :as pi]))

(defn what-is-my-ip [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body (:remote-addr request)})

(defroutes main-routes
  (GET "/" [] "<h1>Bonjour Agile Grenoble !</h1>")
  (GET "/json" [] (pi/json-response {"hello" "world"}))
  (GET "/son2" request (what-is-my-ip request))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> main-routes
    (handler/site)))
