(ns infra.upload
  (:use [net.cgrand.enlive-html :only [deftemplate]]
        [clojure.java.io :only (file copy)]
        hiccup.element 
        hiccup.form
        [hiccup.page :only [include-css html5 ]]))
  
(defn render [t]
      (apply str t))

(deftemplate index "templates/index.html" [])

