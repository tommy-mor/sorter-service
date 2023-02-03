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


(defn method [])

(pco/defresolver tags [env _]
  {::pco/output [{:tags [:tags/id]}]}
  (let [param (pco/params env)]
    ;; TODO 
    {:tags {:items/id (:items/id param)}}))

(pco/defresolver tags [env _]
  {::pco/output [{:tags [:tags/id]}]}
  (let [param (pco/params env)]
    ;; TODO 
    {:tags {:items/id (:items/id param)}}))

(pco/defresolver namespaces [env _]
  {::pco/output [:namespaces]}
  {:namespaces (->> (exec! (-> (h/select-distinct :domain_pk_namespace)
                               (h/from :items)))
                    (map (comp first vals))
                    (filter some?)
                    vec)})

(pco/defresolver items-in-namespace [env _]
  {::pco/output [:items/in-namespace [:items/id]]}
  (let [ns (:ns (pco/params env))]
    {:items/in-namespace (exec! (-> (h/select :id)
                                    (h/from :items)
                                    (h/where [:= :domain_pk_namespace ns])))}))

(def resolvers [namespaces items-in-namespace])


