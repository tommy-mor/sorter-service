(ns ssorter.server_components.migrations
  (:require
   [ssorter.server-components.db :refer [db exec!]]
   [honey.sql.helpers :as h]
   [mount.core :refer [defstate]]
   [migratus.core :as migratus]
   [taoensso.timbre :as log]))

(def config (fn [db] {:store :database
                      :migration-dir "migrations/"
                      :migration-table-name "migrations"
                      :db {:datasource db}}))

(defn run [db]
  (log/info "running migrations")
  (migratus/migrate (config db)))

(comment (clojure.pprint/pprint (->> (exec! (-> (h/select :*)
                                                (h/from :migrations)))
                                     (map :migrations/id)
                                     (drop 3)
                                     reverse
                                     (map #(migratus/down (config db) %)))))
(defstate done
  :start (do
           (run db)))

(comment
  (clojure.pprint/pprint (exec! (-> (h/from :information_schema.tables)
                                    (h/select :*)))))

(comment
  (migratus/create (config db) "initial-rows" :edn)
  (migratus/rollback (config db))
  (migratus/rollback-until-just-after (config db) 20230209231627)
  
  (run))

