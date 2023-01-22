(ns ssorter.server-components.db
  (:require
   [ssorter.server-components.config :refer [config]]
   [mount.core :refer [defstate]]
   [taoensso.timbre :as log]
   [honey.sql :as sql]
   [next.jdbc :as jdbc]))


;; https://www.perrygeo.com/dont-install-postgresql-using-containers-for-local-development.html 



(defstate db
  :start (let [connectstring
               (or (:db/connectstring config)
                   (clojure.edn/read-string (slurp "database/config.edn")))]
           (log/info "starting database connection with connecstring" (pr-str connectstring))
           (jdbc/get-connection connectstring)))

(defn exec! [m] (jdbc/execute! db (sql/format m)))


