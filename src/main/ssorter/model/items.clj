(ns ssorter.model.items
  (:require
   [ssorter.server-components.db :refer [exec!]]
   [taoensso.timbre :as log]
   [honey.sql.helpers :as h]
   [com.wsscode.pathom3.connect.operation :as pco]))

(pco/defresolver items [env input]
  {:items (exec! (-> (h/select :id)
                     (h/from :items)))})

(def resolvers [items])
