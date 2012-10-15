(ns infra.upload
  (:use [net.cgrand.enlive-html :only [deftemplate]]
        [clojure.java.io :only (file copy)]
        [infra.handlers :only (session-maps smaps local-file)]))
  
(defn render [t]
      (apply str t))

(deftemplate index "templates/index.html" [])
(deftemplate sample "templates/sample.html" [])
(deftemplate inclusion "templates/inclusion.html" [])
(deftemplate upload-success "templates/fileupload/success.html" [])

(defn upload-file
  [f-data]
  (copy 
    (f-data :tempfile)
    local-file)
  (dosync ref-set smaps (session-maps))
  (render (upload-success)))

