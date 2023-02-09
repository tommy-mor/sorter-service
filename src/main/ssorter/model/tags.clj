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
   [sluj.core :refer [sluj]]))


(pco/defresolver tags [env _]
  {::pco/output [{:tags [:tags/id]}]}
  (let [param (pco/params env)]
    {:tags (exec! (-> (h/select :id)
                      (h/from :tags)))}))

(pco/defresolver tag [env {:keys [tags/id]}]
  {::pco/input [:tags/id]}
  {::pco/output [:tags/title :tags/description]}
  (first (exec! (-> (h/select :*)
                    (h/from :tags)
                    (h/where [:= :id id])))))

(defn create-tag [new-tag]
  (assert (nil? (:tags/id new-tag)))
  (assoc new-tag :tags/slug (sluj (:tags/title new-tag))))

(pco/defmutation create [new-tag]
  (exec! (-> (h/insert-into :tags)
             (h/values [(create-tag new-tag)])
             (h/returning :id))))

(pco/defmutation update [tag]
  (assert (not (nil? (:tags/id tag))))
  ;; QUESTION do I update slug too?
  (exec! (-> (h/update :tags)
             (h/set (-> tag
                        (assoc :tags/edited_at (java.util.Date.))))
             (h/where [:= :id (:tags/id tag)])
             (h/returning :id))))

(pco/defmutation delete [tag]
  (assert (not (nil? (:tags/id tag))))
  ;; QUESTION do I update slug too?
  (exec! (-> (h/delete-from :tags)
             (h/where [:= :id (:tags/id tag)])
             (h/returning :id))))


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
                create-tag])


