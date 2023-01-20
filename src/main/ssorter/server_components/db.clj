(ns ssorter.server-components.db
  (:require
   [ssorter.server-components.config :refer [config]]
   [mount.core :refer [defstate]]
   [taoensso.timbre :as log]
   
   [migratus.core :as migratus]
   [next.jdbc :as jdbc]
   [honey.sql :as sql]
   [honey.sql.helpers :as h]))


;; https://www.perrygeo.com/dont-install-postgresql-using-containers-for-local-development.html 



(defstate db
  :start (let [connectstring (:db/connectstring config)]
           (jdbc/get-connection {:connection-uri connectstring})))

