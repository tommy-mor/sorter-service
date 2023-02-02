(ns ssorter.pair
  (:require
   [ssorter.server-components.db :refer [exec!]]
   [taoensso.timbre :as log]
   [honey.sql.helpers :as h]
   [com.wsscode.pathom3.connect.built-in.resolvers :as pbir]
   [com.wsscode.pathom3.connect.operation :as pco]

   ;; for test comment block
   [com.wsscode.pathom3.connect.indexes :as pci]
   [com.wsscode.pathom3.interface.eql :as p.eql]))

(defn pair-from-itemids [coll]
  (let [ids (shuffle (map :items/id coll))]
    {:pair/left_item_id (first ids) :pair/right_item_id (second ids)}))

(pco/defresolver pair [env {:keys [:items/in-namespace]}]
  {::pco/output [:pair/left_item_id
                 :pair/right_item_id]}
  (pair-from-itemids in-namespace))

(pco/defresolver pair-items [env {:keys [:pair/left_item_id
                                         :pair/right_item_id]}]
  {::pco/output [{:pair/left [:items/id]}
                 {:pair/right [:items/id]}]}
  {:pair/left {:items/id left_item_id}
   :pair/right {:items/id right_item_id}})

(def resolvers [pair pair-items])
