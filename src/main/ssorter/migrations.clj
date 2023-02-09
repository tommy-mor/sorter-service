(ns ssorter.migrations
  (:require
   [ssorter.server-components.db :refer [db]]
   [migratus.core :as migratus]
   [taoensso.timbre :as log]))

(def config (fn [db] {:store :database
                      :migration-dir "migrations/"
                      :migration-table-name "migrations"
                      :db {:connection db}}))

(defn run []
  (log/info "running migrations")
  (migratus/migrate (config db)))

(comment
  (migratus/create (config db) "tags")
  (migratus/rollback (config db))
  (run))




