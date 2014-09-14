(ns script.propile-client)

(filter :session  (:program_entries (:body (client/get "http://cfp.agile-grenoble.org/programs/5/full_export" {:as :json}))))