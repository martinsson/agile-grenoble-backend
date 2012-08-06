(ns deploy)

(defn- sane-package-manager
  []
  (pallet.resource.package/package-manager :universe)
  (pallet.resource.package/package-manager :multiverse)
  (pallet.resource.package/package-manager :update))

(pallet.core/defnode master
  [:ubuntu :X86_32 :size-id "m1.small"
   :inbound-ports [22 80 443]]
  :bootstrap [(pallet.crate.admin/automated-admin-user +admin-username+)
              (sane-package-manager)]
  :configure [(pallet.crate.java/java :sun)
              (pallet.crate.tomcat/tomcat)
;              (pallet.crate.couchdb/couchdb
;                [:query_server_config :reduce_limit] "false"
;                [:couchdb :database_dir] +couchdb-root+)
;              (pallet.resource.directory/directory +couchdb-root+
;                :owner "couchdb:couchdb" :mode 600)
]
  :deploy [(pallet.resource.service/with-restart "tomcat*"
             (pallet.crate.tomcat/deploy-local-file "/path/to/my/warfile.war" "ROOT"))])

;(def service (jcompute/compute-service "ec2" "AWS_ID" "AWS_SECRET_KEY" :ssh :log4j)

(pallet.core/with-admin-user [+admin-username+]
 ; (jcompute/with-compute-service [service]
    (pallet.core/converge {master 1} :configure :deploy))