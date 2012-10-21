(ns infra.upload
  (:use [net.cgrand.enlive-html :only [deftemplate]]
        [clojure.java.io :only (file copy)]
        [infra.handlers :only (session-maps smaps local-file)]
        hiccup.element 
        hiccup.form
        [hiccup.page :only [include-css html5 ]]))
  
(defn render [t]
      (apply str t))

(deftemplate index "templates/index.html" [])
(deftemplate sample "templates/sample.html" [])
(deftemplate upload-success "templates/fileupload/success.html" [])
(defn login-form []
  (form-to [:post "/login"]          
           (text-field {:placeholder "username"} "username")                       
           (password-field {:placeholder "password"} "password")                       
           (submit-button "login")))

(defn login [] 
  (hiccup.core/html (html5
    [:head
     [:title "login"]]
    [:body              
     [:div.login
       (login-form)  ]])))

(defn upload-file
  [f-data]
  (copy 
    (f-data :tempfile)
    local-file)
  (dosync ref-set smaps (session-maps))
  (render (upload-success)))

