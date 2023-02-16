(ns ssorter.model.items
  (:require
   [ssorter.server-components.db :refer [exec!]]
   [ssorter.model.utils :as utils]
   
   [taoensso.timbre :as log]
   [honey.sql.helpers :as h]
   [com.wsscode.pathom3.connect.built-in.resolvers :as pbir]
   [com.wsscode.pathom3.connect.operation :as pco]

   ;; for test comment block
   [com.wsscode.pathom3.connect.indexes :as pci]
   [com.wsscode.pathom3.interface.eql :as p.eql]
   [sluj.core :refer [sluj]]))

(pco/defresolver items [env _]
  {:items (exec! (-> (h/select :id)
                     (h/from :items)))})

(pco/defresolver item-by-id [env _]
  {::pco/output [{:item [:items/id]}]}
  (let [params (pco/params env)]
    {:query.items/by-id {:items/id (:items/id params)}}))

(pco/defresolver item [env {:keys [:items/id]}]
  {::pco/output [:items/slug
                 :items/domain_pk
                 :items/title
                 :items/domain_pk_namespace
                 :items/id
                 :items/url
                 :items/access
                 :items/edited_at
                 :items/body
                 :items/created_at]}
  (first (exec! (-> (h/select :*)
                    (h/from :items)
                    (h/where [:= :id id])))))

;; mutations


(defn create-item [new-item]
  (assert (nil? (:items/id new-item)))
  
  (assoc new-item
         :items/slug (sluj (:items/title new-item))))

(pco/defmutation create [{:items/keys [title body url domain_pk domain_pk_namespace] :as new-item}]
  (exec! (-> (h/insert-into :items)
             (h/values [(create-item new-item)])
             (h/returning :id))))

(pco/defmutation create-many [items]
  (when (not-empty items)
    (exec! (-> (h/insert-into :items)
               (h/values (map create-item items))
               (h/returning :id)))))

(pco/defmutation update [item]
  (let [item (utils/fill-out-id :items item)]
    (assert (not (nil? (:items/id item))))

    (def x item)
    (first (exec! (-> (h/update :items)
                      (h/set (-> item
                                 (dissoc :items/id)
                                 (assoc :items/edited_at (java.util.Date.))))
                      (h/where [:= :id (:items/id item)])
                      (h/returning :id))))))

(pco/defmutation delete [item]
  (let [item (utils/fill-out-id :items item)
        id (:items/id item)]
    
       (assert (:items/id item))
       
       ;; TODO delete votes referencing this item?
       (exec! (-> (h/delete-from :items_in_tags)
                  (h/where [:= :item_id id])
                  (h/returning :tag_id)))
       
       (exec! (-> (h/delete-from :items)
                  (h/where [:= :id id])
                  (h/returning :id)))))

(def resolvers [items item item-by-id create create-many delete])

(comment
  (items {} {})
  (def r (p.eql/process (pci/register resolvers)
                        `[(create {:items/title "epic item!!!"
                                   :items/body "item body :smirk:"
                                   :items/domain_pk 1
                                   :items/domain_pk_namespace "test"})]))

  (def item-id  (-> (get r `create)
                    first
                    :items/id))
  (item {} {:items/id item-id})
  (update {:items/id item-id :items/title "not as epic anymore"})
  (item {} {:items/id item-id})
  (delete {:items/id item-id}))
