(ns ssorter.server-components.db
  (:require
   [ssorter.server-components.config :refer [config]]
   [mount.core :refer [defstate]]
   [taoensso.timbre :as log]
   
   [next.jdbc :as jdbc]))


;; https://www.perrygeo.com/dont-install-postgresql-using-containers-for-local-development.html 



(defstate db
  :start (let [connectstring
               (or (:db/connectstring config)
                   (clojure.edn/read-string (slurp "database/config.edn")))]
           (jdbc/get-connection connectstring)))


