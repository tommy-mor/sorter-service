(ns ssorter.model.tags
  (:require
   [ssorter.server-components.db :refer [exec!]]
   [taoensso.timbre :as log]
   [honey.sql.helpers :as h]
   [com.wsscode.pathom3.connect.built-in.resolvers :as pbir]
   [com.wsscode.pathom3.connect.operation :as pco]

   ;; for test comment block
   [com.wsscode.pathom3.connect.indexes :as pci]
   [com.wsscode.pathom3.interface.eql :as p.eql]
   
   [ssorter.model.membership :as m.membership]
   [ssorter.model.items :as m.items]
   [ssorter.model.votes :as m.votes]
   [sluj.core :refer [sluj]]))


(pco/defresolver tags [env _]
  {::pco/output [{:tags [:tags/id]}]}
  (let [param (pco/params env)]
    {:tags (exec! (-> (h/select :id)
                      (h/from :tags)))}))

(comment (tags))

(pco/defresolver tag-param [env _]
  {::pco/output [:tags/id]}
  (let [params (pco/params env)
        tagid (:tags/id params)]
    (when tagid
      {:tags/id tagid})))

(pco/defresolver tag [env {:keys [:tags/id]}]
  {::pco/input [:tags/id]
   ::pco/output [:tags/title :tags/description]}
  
  (first (exec! (-> (h/select :*)
                    (h/from :tags)
                    (h/where [:= :id id])))))

(comment (tag {:tags/id 37}))

(defn create-tag [new-tag]
  (assert (nil? (:tags/id new-tag)))
  (assoc new-tag :tags/slug (sluj (:tags/title new-tag))))

(pco/defmutation create [new-tag]
  (exec! (-> (h/insert-into :tags)
             (h/values [(create-tag new-tag)])
             (h/returning :id))))

(pco/defmutation update [tag]
  (assert (not (nil? (:tags/id tag))))
  ;; TODO use util method to update with just domainpk
  ;; QUESTION do I update slug too?
  (exec! (-> (h/update :tags)
             (h/set (-> tag
                        (assoc :tags/edited_at (java.util.Date.))))
             (h/where [:= :id (:tags/id tag)])
             (h/returning :id))))

(defn tag-garbage [tag]
  (def orphaned-items (exec! (-> (h/from [:items :i1])
                                 (h/select :i1.id)
                                 (h/where [:in :i1/id (-> (h/select-distinct :item_id)
                                                          (h/from :items_in_tags)
                                                          (h/where [:= :tag_id (:tags/id tag)]))])
                                 (h/left-join :items_in_tags
                                              [:= :items_in_tags/item_id :i1/id])
                                 (h/group-by :i1/id)
                                 (h/having [:= 1 :%count.items_in_tags/tag_id]))))
  
  (def orphaned-votes
    (if (empty? orphaned-items)
      []
      (exec! (-> (h/from :votes)
                 (h/select :id)
                 (h/where :or
                          [:in :votes/left_item_id (map :items/id orphaned-items)]
                          [:in :votes/right_item_id (map :items/id orphaned-items)])))))
  
  (def orphaned-memberships
    (if (empty? orphaned-items)
      []
      (->> (exec! (-> (h/from :items_in_tags)
                      (h/select :tag_id :item_id)
                      (h/where [:= :items_in_tags/tag_id (:tags/id tag)]
                               [:in :items_in_tags/item_id
                                (map :items/id orphaned-items)])))
           (map #(clojure.set/rename-keys % {:items_in_tags/item_id :items/id
                                             :items_in_tags/tag_id :tags/id})))))
  {:orphaned-items orphaned-items
   :orphaned-votes orphaned-votes
   :orphaned-memberships orphaned-memberships})
  
  
  
(pco/defmutation delete [tag]
  (assert (not (nil? (:tags/id tag))))
  
  (log/info "Deleting tag" tag)
  (when (::cascade? tag)
    (let [{:keys [orphaned-items orphaned-votes orphaned-memberships]} (tag-garbage tag)]
      
      (doall (map m.membership/unenroll-item orphaned-memberships))
      
      (log/info "Deleting orphaned votes" (count orphaned-votes))
      (doall (map m.votes/delete orphaned-votes))
      
      (log/info "Deleting orphaned items" (count orphaned-items))
      (doall (map m.items/delete orphaned-items))))
  
  (exec! (-> (h/delete-from :tags)
             (h/where [:= :id (:tags/id tag)]))))


(comment
  (create-tag {:tags/title "epic tag"})
  (create {:tags/title "example tag"})
  (tag {:tags/id 1})
  (update {:tags/id 1 :tags/title "swage"})
  (delete {:tags/id 1})
  (tags))

(pco/defresolver namespaces [env _]
  {::pco/output [:namespaces]}
  {:namespaces (->> (exec! (-> (h/select-distinct :domain_pk_namespace)
                               (h/from :items)))
                    (map (comp first vals))
                    (filter some?)
                    vec)})

(pco/defresolver items-in-namespace [env _]
  {::pco/output [:items/in-namespace [:items/id]]}
  (let [ns (:items/domain_pk_namespace (pco/params env))]
    {:items/in-namespace (exec! (-> (h/select :id)
                                    (h/from :items)
                                    (h/where [:= :domain_pk_namespace ns])))}))

(pco/defresolver votes-in-namespace [env _]
  {::pco/output [:votes/in-namespace [:votes/id]]}
  (let [ns (:ns (pco/params env))]
    {:votes/in-namespace (exec! (-> (h/select :id)
                                    (h/from :votes)
                                    (h/where [:= :domain_pk_namespace ns])))}))

(def resolvers [namespaces
                items-in-namespace
                votes-in-namespace
                
                tag-param
                tags tag create update delete])


