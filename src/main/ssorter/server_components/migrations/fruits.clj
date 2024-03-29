(ns ssorter.server-components.migrations.fruits
  (:require
   [ssorter.server-components.db :refer [db exec!]]
   [next.jdbc :as jdbc]
   [taoensso.timbre :as log]
   [honey.sql :as sql]
   [honey.sql.helpers :as h]
   [crypto.password.bcrypt :as password]
   [ssorter.model.groups]
   [ssorter.server-components.migrations.old-importer :as old]))

(defn gather-from-json [id]
  
  (exec! (-> (h/insert-into :items)
             (h/values
              (for [item (old/good-items-for id)]
                {:access 0
                 :title item
                 :slug item}))))
  (let [lookup (fn [title] (-> (jdbc/execute! db ["select id from items where title = ?" title]) first :items/id))]
    (exec! (-> (h/insert-into :votes)
               (h/values  (for [{:keys [left_item right_item magnitude]} (old/good-votes-for old/fruits-id)]
                            {:left_item_id (lookup left_item)
                             :right_item_id (lookup right_item)
                             :magnitude magnitude
                             :access 0
                             :attribute 0})))))
  "TODO import tags"
  "TODO make item insert idempotent (insert and look for tag)")

(defn fill-database []

  (exec! (-> (h/insert-into :users)
             (h/values [{:user_name "tommy"
                         :email "thmorriss@gmail.com"
                         :password_hash
                         "$2a$11$Lrwqg86XlBahlQOhDlI9EeM5Gfdniw19M5Bcer0gCQKqy1biVtAKS"}])

             (h/upsert (-> (h/on-conflict :user_name)
                           (h/do-nothing)))))
  (exec! (-> (h/insert-into :access)
             (h/values [{:id 0 ;; this will not be zero eventually, this record will be generated by build-user! funciton
                         :user_id "tommy"
                         :inherit false
                         :owner true
                         :can_edit true
                         :can_read true}])
             (h/upsert (-> (h/on-conflict :id)
                           (h/do-nothing)))))
  (exec! (-> (h/insert-into :attributes)
             (h/values [{:id 0
                         :access 0 ;; TODO make public
                         :title "like"
                         :description "how much do you like something. analagous to likes on other platforms"}])
             (h/upsert (-> (h/on-conflict :id)
                           (h/do-nothing)))))

  (gather-from-json old/fruits-id))

(defn up [x]
  (def x {:store :database, :migration-dir "migrations/",
          :migration-table-name "migrations",
          :db {:datasource nil},
          :conn nil})
  (fill-database))

(defn down [y]
  (exec! (-> (h/delete-from :attributes)
             (h/where [:= :id 0])))
  
  (exec! (-> (h/delete-from :access)
             (h/where [:= :id 0])))
  
  (exec! (-> (h/delete-from :users)
             (h/where [:= :user_name "tommy"]))))
