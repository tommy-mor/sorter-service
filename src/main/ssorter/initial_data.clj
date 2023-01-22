(ns ssorter.initial-data
  (:require
   [ssorter.server-components.db :refer [db]]
   [next.jdbc :as jdbc]
   [taoensso.timbre :as log]
   [honey.sql :as sql]
   [honey.sql.helpers :as h]
   [crypto.password.bcrypt :as password]
   [ssorter.model.groups]))

(comment (jdbc/execute! db (sql/format (-> (h/select :*)
                                           (h/from :users)))))

(defn exec! [m] (jdbc/execute! db (sql/format m)))

(defn fill-database []
  (exec! (-> (h/insert-into :users)
             (h/values [{:user_name "tommy"
                         :email "thmorriss@gmail.com"
                         :password_hash "$2a$11$7upXjLlYoBpMwkJXKska7usiBcd3j.bL7BTjr7F4gywYuO/KT0sE2"}])

             (h/upsert (-> (h/on-conflict :user_name)
                           (h/do-nothing)))))
  (exec! (-> (h/insert-into :access)
             (h/values [{:id 0
                         :user_id "tommy"
                         :inherit false
                         :owner true
                         :can_edit true
                         :can_read true}]))))
