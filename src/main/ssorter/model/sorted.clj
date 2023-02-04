(ns ssorter.model.sorted
  (:require
   [ssorter.server-components.db :refer [exec!]]
   [taoensso.timbre :as log]
   [honey.sql.helpers :as h]
   [honey.sql :as hq]
   [com.wsscode.pathom3.connect.built-in.resolvers :as pbir]
   [com.wsscode.pathom3.connect.operation :as pco]
   
   [ssorter.rank :as rank]

   ;; for test comment block
   [com.wsscode.pathom3.connect.indexes :as pci]
   [com.wsscode.pathom3.interface.eql :as p.eql]
   [clojure.set]))

(defn sorted-by-ids [ids]
  (def votes (exec! (-> (h/select :left_item_id :right_item_id :magnitude :id :attribute)
                        (h/from :votes)
                        (h/where [:in :votes.left_item_id ids]
                                 [:in :votes.right_item_id ids]))))
  (def valid-ids (->> votes
                      (map (juxt :votes/left_item_id :votes/right_item_id))
                      flatten distinct set))
  {:sorted/sorted
   (vec (for [[item score] (rank/sorted (map #(hash-map :items/id %) valid-ids) votes)]
          (assoc item :items/score score)))
   :sorted/unsorted-ids (clojure.set/difference (set ids) valid-ids)})

(pco/defresolver by-pks
  "params. :ids for list of ids. if you include the ns tag,
   it gathers its rank from entire tag, not just your subset."
  [env _]
  {::pco/output [{:sorted/by-pks [{:sorted/sorted [:items/id :items/score]}
                                  :sorted/unsorted-ids]}]}
  (let [params (pco/params env)]
    (def params params)
    (def pks (:items/domain_pks params))
    (def namespace (:items/domain_pk_namespace params))
    (def itemids (exec! (-> (h/select :id)
                            (h/from :items)
                            (h/where [:in :items.domain_pk pks]
                                     [:= :items.domain_pk_namespace namespace]))))
    
    {:sorted/by-pks (sorted-by-ids (map :items/id itemids))}))

(pco/defresolver by-ids
  "params. :ids for list of ids. if you include the ns tag,
   it gathers its rank from entire tag, not just your subset."
  [env _]
  {::pco/output [{:sorted/by-ids [{:sorted/sorted [:items/id :items/score]}
                                  :sorted/unsorted-ids]}]}
  {:sorted/by-ids (sorted-by-ids (:ids (pco/params env)))})


(def resolvers [by-ids by-pks])
