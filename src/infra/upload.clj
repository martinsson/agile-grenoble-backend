(ns infra.upload
  (:use [net.cgrand.enlive-html :only [deftemplate]]
        [clojure.java.io :only (file copy)])
  (:require (ring.middleware [multipart-params :as mp])))
  
(defn render [t]
      (apply str t))

(deftemplate index "templates/index.html" [])
(deftemplate upload-success "templates/fileupload/success.html" [])

(defn upload-file
  [f-data]
  (copy 
    (f-data :tempfile)
    (file "resources/public/uploaded-sessions.csv"))
  (render (upload-success)))

