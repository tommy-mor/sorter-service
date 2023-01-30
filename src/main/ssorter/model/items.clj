(ns ssorter.model.items
  (:require
   [ssorter.server-components.db :refer [exec!]]
   [taoensso.timbre :as log]
   [honey.sql.helpers :as h]
   [com.wsscode.pathom3.connect.operation :as pco]
   [com.wsscode.pathom3.connect.built-in.resolvers :as pbir]))

(pco/defresolver items [env _]
  {:items (exec! (-> (h/select :id)
                     (h/from :items)))})

(pco/defresolver item [env _]
  {::pco/output [{:item [:items/id]}]}
  (let [param (pco/params env)]
    {:item {:items/id (:items/id param)}}))

(pco/defresolver item-fields [env {:keys [:items/id]}]
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

(def resolvers [items item item-fields])
