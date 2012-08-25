(ns infra.http  
  (:use compojure.core)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [core.program-import :as pi]
            [core.adding-missing-data :as amd]
            [clj-json.core :as json]))


(defn wrap-with-content-type-json [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Content-Type"] "application/json"))))

(defn json-encode [handler]
  (fn [request]
    (let [response (handler request)]
      (update-in response [:body] json/generate-string))))

(def core-handler
  (-> pi/get-session-3
     (json-encode)
     (wrap-with-content-type-json)))

(def session-list
  (-> amd/sessions-with-missing-data
     (json-encode)
     (wrap-with-content-type-json)))

(defroutes main-routes
  (GET "/" [] "<h1>Bonjour Agile Grenoble !</h1>")
  (GET "/json" request (core-handler request))
  (GET "/session-list" request (session-list request))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> main-routes
    (handler/site)))
