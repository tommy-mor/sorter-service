(ns ssorter.server-components.db
  (:require
   [ssorter.server-components.config :refer [config]]
   [mount.core :refer [defstate]]
   [taoensso.timbre :as log]
   [honey.sql :as sql]
   [next.jdbc :as jdbc]
   [next.jdbc.date-time]
   [hikari-cp.core :refer [make-datasource close-datasource]]))


;; https://www.perrygeo.com/dont-install-postgresql-using-containers-for-local-development.html 



(defn parse-jdbc [st]
  (drop 1 (re-matches
           #"postgres://(.*):(.*)@(.*):(\d*)/(.*)"
           
           st)))

(comment
  (parse-jdbc (:jdbc-url (clojure.edn/read-string (slurp "database/config.edn"))))
  (parse-jdbc "postgres://doadmin:AVNS_fhlhM10kDKeczNIDTRY@db-postgresql-nyc1-50127-do-user-911048-0.b.db.ondigitalocean.com:25060/sorter")
  
  )

(defstate db
  :start (let [cfg
               (or (:db/connectstring config)
                   (clojure.edn/read-string (slurp "database/config.edn")))

               [username password host port database] (parse-jdbc (:jdbc-url cfg))
               with-defaults (merge {:auto-commit        true
                                     :read-only          false
                                     :connection-timeout 30000
                                     :validation-timeout 5000
                                     :idle-timeout       600000
                                     :max-lifetime       1800000
                                     :minimum-idle       10
                                     :maximum-pool-size  10
                                     :pool-name          "db-pool"
                                     :register-mbeans false
                                     :adapter "postgresql"
                                     :username username
                                     :password password
                                     :server-name host
                                     :port-number (Integer/parseInt port)
                                     :database-name database}
                                    cfg)]
           (def with-defaults with-defaults)
           (assert (:jdbc-url cfg))
           (log/info "starting database connection with connecstring" (pr-str with-defaults))
           (make-datasource with-defaults))
  :stop (close-datasource db))

(defn exec! [m]
  (def m m)
  (with-open [conn (jdbc/get-connection {:datasource db})]
    (jdbc/execute! conn (cond (map? m)
                              (sql/format m)
                              (string? m)
                              [m]

                              (vector? m)
                              m))))



