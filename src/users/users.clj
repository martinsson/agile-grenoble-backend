(ns users.users
  (:require [clojure.java.io :as io]
            [cemerick.friend.credentials :as creds]))

(defn load-props
  [file-name]
  (with-open [^java.io.Reader reader (io/reader file-name)] 
    (let [props (java.util.Properties.)]
      (.load props reader)
      (into {} (for [[k v] props] [k v])))))

(def creds (load-props (str (System/getProperty "user.home") "/.sessions/credentials.properties")))
(def users {"admin" {:username "admin"
                    :password (creds/hash-bcrypt (creds "admin"))
                    :roles #{::admin}}})

