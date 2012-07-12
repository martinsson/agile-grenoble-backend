(ns infra.http  
  (:use compojure.core)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [core.program-import :as pi]))


(defroutes main-routes
  (GET "/" [] "<h1>Bonjour Agile Grenoble !</h1>")
  (GET "/json" [] (pi/json-response {"hello" "world"}))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (handler/site main-routes))