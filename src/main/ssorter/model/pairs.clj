(ns ssorter.model.pairs
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

(pco/defresolver pair-for-namespace [env {:keys [:items/in-namespace]}]
  {::pco/output [:pair/left_item_id
                 :pair/right_item_id]}
  (pair-from-itemids in-namespace))

(pco/defresolver pair-for-itemids [env _]
  {::pco/output [{:pair/from-ids [:pair/left_item_id
                                  :pair/right_item_id]}]}
  

  (let [{:keys [pks items/domain_pk_namespace]} (pco/params env)]
    (def pks pks)
    (def domain_pk_namespace domain_pk_namespace)
    {:pair/from-ids
     (pair-from-itemids (exec! (-> (h/select :id)
                                   (h/from :items)
                                   (h/where [:in :items.domain_pk (map :items/domain_pk pks)]
                                            [:= domain_pk_namespace :items.domain_pk_namespace]))))}))

(pco/defresolver pair-items [env {:keys [:pair/left_item_id
                                         :pair/right_item_id]}]
  {::pco/output [{:pair/left [:items/id]}
                 {:pair/right [:items/id]}]}
  {:pair/left {:items/id left_item_id}
   :pair/right {:items/id right_item_id}})

(pco/defresolver sorted-pair [env {:keys [:sorted/id :sorted/unsorted :sorted/sorted]}]
  {::pco/output [{:sorted/pair [:pair/letf_item_id :pair/right_item_id]}]}
  
  (def x unsorted)
  (def y sorted)
  {:sorted/pair {:pair/left_item_id 0 :pair/right_item_id 1}})

(pco/defresolver tag-pair [env {:keys [:tags/sorted]}]
  {::pco/output [{:tags/pair [:pair/letf_item_id :pair/right_item_id]}]}
  (def sorted sorted)

  {:tags/pair (merge {:sorted/id (:sorted/id sorted)}
                     (pair-from-itemids (concat (:sorted/unsorted sorted)
                                                (:sorted/sorted sorted))))})


(def resolvers [pair-for-namespace
                pair-for-itemids
                pair-items
                sorted-pair
                tag-pair])
