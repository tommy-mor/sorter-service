(ns ssorter.migrations
  (:require
   [ssorter.server-components.db :refer [db]]
   [migratus.core :as migratus]
   [taoensso.timbre :as log]))

(def config {:store :database
             :migration-dir "migrations/"
             :migration-table-name "migrations"
             :db {:connection db}})

(defn run []
  (log/info "running migrations")
  (migratus/migrate config))

(comment
  (migratus/create config "initial")
  (run))




