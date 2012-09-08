(ns infra.upload
  (:use [net.cgrand.enlive-html
              :only [deftemplate defsnippet content clone-for
                     nth-of-type first-child do-> set-attr sniptest at emit*]]
        [clojure.java.io :only (file copy)])
  (:require (ring.middleware [multipart-params :as mp])))
  
(defn render [t]
      (apply str t))

(deftemplate index "fileupload/index.html" [])
(deftemplate upload-success "fileupload/success.html" [])

(defn upload-file
  [tempfile]
  (copy 
    (tempfile :tempfile) 
    (file "resources/public/uploaded-sessions.csv"))
  (render (upload-success)))

