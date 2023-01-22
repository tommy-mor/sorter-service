(ns ssorter.queries
  (:require
   [ssorter.server-components.db :refer [db exec!]]
   [next.jdbc :as jdbc]
   [taoensso.timbre :as log]
   [honey.sql.helpers :as h]))

(comment
  (exec! (-> (h/select :*)
             (h/from :items))))
