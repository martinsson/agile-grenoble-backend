(ns agile-grenoble-backend.core  
  (:use compojure.core)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [clj-json.core :as json]))

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string data)})

(defroutes main-routes
  (GET "/" [] "<h1>Bonjour Agile Grenoble !</h1>")
  (GET "/json" [] (json-response {"hello" "world"}))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (handler/site main-routes))