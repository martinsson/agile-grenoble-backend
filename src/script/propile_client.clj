(ns script.propile-client)
(defonce propile-response (client/get "http://cfp.agile-grenoble.org/programs/5/full_export" {:as :json}))

(->> propile-response 
  :body 
  :program_entries 
  (filter :session))