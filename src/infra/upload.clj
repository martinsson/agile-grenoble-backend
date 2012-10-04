(ns infra.upload
  (:use [net.cgrand.enlive-html :only [deftemplate]]
        [clojure.java.io :only (file copy)]
        [infra.handlers :only (session-maps decorate-sessions)]
        [core.program-import :only (keep-retained)]))
  
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
    (file (str (System/getProperty "user.home") "/uploaded-sessions.csv")))
  (dosync ref-set session-maps (keep-retained (decorate-sessions (file (str (System/getProperty "user.home") "/uploaded-sessions.csv")))))
  (render (upload-success)))

