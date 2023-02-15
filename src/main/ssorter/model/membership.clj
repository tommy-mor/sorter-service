(ns ssorter.model.membership
  (:require
   [ssorter.server-components.db :refer [exec!]]
   [taoensso.timbre :as log]
   [honey.sql.helpers :as h]
   [com.wsscode.pathom3.connect.built-in.resolvers :as pbir]
   [com.wsscode.pathom3.connect.operation :as pco]

   ;; for test comment block
   [com.wsscode.pathom3.connect.indexes :as pci]
   [com.wsscode.pathom3.interface.eql :as p.eql]
   [sluj.core :refer [sluj]]))

(pco/defresolver tag-members [env {:keys [tags/id]}]
  {::pco/input [:tags/id]
  ::pco/output [{:tags/members [:items/id]}]}
  {:tags/members (->> (exec! (-> (h/select :*)
                                 (h/from :items_in_tags)
                                 (h/where [:= :tag_id id])))
                      (map #(hash-map ::status (:items_in_tags/status %)
                                      :items/id (:items_in_tags/item_id %)))
                      vec)})

(pco/defresolver item-memberships [env {:keys [:items/id]}]
  {::pco/input [:tags/id]
   ::pco/output [{:items/memberships [:tags/id]}]}
  {:items/memberships (exec! (-> (h/select :*)
                                 (h/from :items_in_tags)
                                 (h/where [:= :item_id id])))})

(defn translate-keywords [itemidtagid]
  {:items_in_tags/tag_id (:tags/id itemidtagid)
   :items_in_tags/item_id (:items/id itemidtagid)
   :items_in_tags/status (or (::status itemidtagid) 0)})

(pco/defmutation enroll-item [itemidtagid]
  (exec! (-> (h/insert-into :items_in_tags)
             (h/values [(translate-keywords itemidtagid)]))))

(pco/defmutation enroll-many-items [items]
  (exec! (-> (h/insert-into :items_in_tags)
             (h/values (map translate-keywords items)))))


(def resolvers [tag-members item-memberships enroll-item])
