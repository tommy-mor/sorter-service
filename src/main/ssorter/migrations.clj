(ns ssorter.migrations
  (:require
   [ssorter.server-components.db :refer [db]]
   [migratus.core :as migratus]
   [next.jdbc :as jdbc]
   [taoensso.timbre :as log]
   [honey.sql :as sql]
   [honey.sql.helpers :as h]))

(def config {:store :database
             :migration-dir "migrations/"
             :migration-table-name "migrations"
             :db {:connection db}})

(defn run []
  (log/info "running migrations")
  (migratus/migrate config))

(comment
  (migratus/create config "items")
  (run))




