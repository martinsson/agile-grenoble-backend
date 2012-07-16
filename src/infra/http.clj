(ns infra.http  
  (:use compojure.core)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [core.program-import :as pi]
            [clj-json.core :as json]))

(defn say-hello [request]
  {:status 200
   :body ["hello" (:to (:params request))]})

(defn json-encode [handler]
  (fn [request]
    (let [response (handler request)]
      (merge response 
             {:headers {"Content-Type" "application/json"}
              :body (json/generate-string (:body response))}))))

(def core-handler
  (-> say-hello
     (json-encode)))

(defroutes main-routes
  (GET "/" [] "<h1>Bonjour Agile Grenoble !</h1>")
  (GET "/json" request (core-handler request))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> main-routes
    (handler/site)))
