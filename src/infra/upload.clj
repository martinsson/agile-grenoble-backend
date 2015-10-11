(ns infra.upload
  (:use [net.cgrand.enlive-html :only [deftemplate]]
        [clojure.java.io :only (file copy)]
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
(defn upload-form []
  (form-to {:enctype "multipart/form-data"}
           [:post "/upload/sessions-csv"]          
           (file-upload :file)
           [:br]
           (submit-button "submit")))

(defn login [] 
  (hiccup.core/html (html5
    [:head
     [:title "login"]]
    [:body              
     [:div.login
       (login-form)  ]])))

(defn upload [] 
  (hiccup.core/html (html5
    [:head
     [:title "upload"]]
    [:body
     [:div.doc [:pre "
La référence est le formulaire de soumissions Google doc

Faire les nos modifs sur le google doc.
  Ex inverser deux sessions : Il y a une colonne “slot”, il faut inverser les valeurs
  Ex Changer de salle : il y a une colonne “room”, il faut la changer
  Ex Annuler une session : il y a une colonne “Retenu = x” dont la valeur est x pour les sessions retenus.

docs.google.com : File → Download as → csv
Ici             : Choose file → Submit"]]
     [:div.upload
       (upload-form)  ]])))
