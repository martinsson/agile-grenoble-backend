(ns core.program-import
  (:use clojure-csv.core)
  (:require [clj-json.core :as json]))

(def sessions 
  (slurp "resources/public/sessions.csv"))


(defn get-session-3 [request]
  {:status 200
   :body (nth (parse-csv sessions) 3) })

