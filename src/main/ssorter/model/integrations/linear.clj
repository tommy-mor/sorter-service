(ns ssorter.model.integrations.linear
  (:require [org.httpkit.client :as http]
            [org.httpkit.sni-client :as sni-client]
            
            [ssorter.server-components.db :refer [exec!]]
            [ssorter.server-components.config :refer [config]]
            
            [ssorter.model.tags :as m.tags]
            [ssorter.model.membership :as m.membership]
            [ssorter.model.items :as m.items]

            [ssorter.sync :as sync]
            
            [taoensso.timbre :as log]
            [honey.sql.helpers :as h]
            [honey.sql :as sql]
            [com.wsscode.pathom3.connect.built-in.resolvers :as pbir]
            [com.wsscode.pathom3.connect.operation :as pco]

            [com.wsscode.pathom3.connect.indexes :as pci]
            [com.wsscode.pathom3.interface.eql :as p.eql]
            
            [graphql-query.core :refer [graphql-query]]

            [jsonista.core :as j]
            [clojure.walk :refer [prewalk]]
            [clojure.set]
            [clojure.string :as str]
            [tick.core :as t])
  
  (:import [org.postgresql.util PSQLException]))

(def mapper
  (j/object-mapper
   {:encode-key-fn name
    :decode-key-fn keyword}))

(def linear-api "https://api.linear.app/graphql")
(def linear-key (:linear/api (clojure.edn/read-string (slurp "secrets.edn"))))

(alter-var-root #'org.httpkit.client/*default-client* (fn [_] sni-client/default-client))

(defn linear-req [m & [arg]]
  (try (let [req @(http/request {:method :post
                                 :url linear-api
                                 :headers {"Content-Type" "application/json"
                                           "Authorization" linear-key}
                                 :body (j/write-value-as-string (merge {:query
                                                                        (if (string? m)
                                                                          m
                                                                          (graphql-query
                                                                           m))}
                                                                       arg))})
             body (:body req)]
         (j/read-value body mapper))
       (catch Exception e
         e)))


(defn wrap-keywords [coll]
  (prewalk (fn [x] (if (keyword? x)
                     (keyword "ssorter.model.integrations.linear" (name x ))
                     x))
           coll))

(comment (wrap-keywords {:x/x 4}))

(def important-fields [:id :title :identifier :description :url])

(defn get-single-issue [{::keys [id]}]
  (-> (linear-req {:queries [[:issue {:id id} important-fields]]})
      :data :issue))

(comment (get-single-issue params))

(defn important-fieds->record [fields]
  (def fields fields)
  {:items/body (:description fields)
   :items/url (:url fields)
   :items/title (str "linear/"(:identifier fields) " - " (:title fields))
   :items/domain_pk (:id fields)
   :items/domain_pk_namespace "linear.issue"
   :items/access 0})

(defn sync-linear-parent-with-tag [{tagid :tags/id linearid ::id}]
  (def tagid tagid)
  (def linearid linearid)

  (def linear-ids (->> (linear-req {:queries [[:issue {:id linearid}
                                               [[:children [[:nodes [:id :updatedAt]]]]]]]})
                       :data :issue :children :nodes
                       (map (juxt :id (comp t/inst :updatedAt)))
                       (into {})))

  (when (empty? linear-ids)
    (throw (ex-info "no subissues" {::id linearid :tags/id tagid})))

  (def existing-members ((fn [] (->> (m.membership/tag-members {:tags/id tagid})
                                     :tags/members
                                     (map m.items/item)))))

  (def existinglinearid->edited (into {} (map (juxt :items/domain_pk :items/edited_at) existing-members)))

  (let [{new-from-linear :left-only
         obsolete-items :right-only
         needs-updating :middle-left-newer}
        (sync/split-by-inst linear-ids existinglinearid->edited)

        to-fetch (clojure.set/union new-from-linear needs-updating)]
    
    (def issue-data (->> (linear-req {:queries [[:issues {:filter {:id {:in to-fetch}}}
                                                 [[:nodes
                                                   important-fields]]]]})
                         :data :issues :nodes
                         (map important-fieds->record)))

    (def new-from-linear new-from-linear)
    (def needs-updating needs-updating)
    (def obsolete-items obsolete-items)

    (def itemids (m.items/create-many
                  (->> issue-data
                       (filter (comp new-from-linear :items/domain_pk)))))
    
    (doall (->> issue-data
                (filter (comp needs-updating :items/domain_pk))
                (map m.items/update)))

    (doall (->> obsolete-items
                (map #(hash-map :items/domain_pk %))
                (map m.items/delete)))

    (when (not-empty itemids)
      (m.membership/enroll-many-items (map #(assoc % :tags/id tagid) itemids) ))
    
    itemids))

(pco/defmutation start-sorting-issue [params]
  (def params params)
  (def issue (get-single-issue params))
  (def tagid (-> (m.tags/create {:tags/title (str "linear.issue/" (:identifier issue) " - " (:title issue))
                                 :tags/description (:description issue)
                                 :tags/domain_pk (::id params)
                                 :tags/domain_pk_namespace "linear.issue"
                                 :tags/domain_url (:url issue)})
                 first
                 :tags/id))
  (comment (exec! (-> (h/delete-from :tags)
                      (h/where true))))

  (sync-linear-parent-with-tag {:tags/id tagid ::id (::id params)})

  "TODO make linear things show up in ui (query for domain_pk in tags...)"
 

  {:tags/id tagid})

(pco/defresolver sorted-issues [env _]
  {::pco/output [{::sorted-issues [::id
                                   :tags/id]}]}
  
  {::sorted-issues (->> (exec! (-> (h/select :id :domain_pk)
                                   (h/from :tags)
                                   (h/where [:= :domain_pk_namespace "linear.issue"])))
                        (map #(clojure.set/rename-keys % {:tags/domain_pk ::id})))})

(pco/defresolver issues [env _]
  {::pco/output [{::issues [::id
                            ::title
                            ::description
                            ::createdAt
                            ::estimate
                            ::priorityLabel
                            ::children
                            ::identifier
                            :tags/id]}]}
  (let [{:keys [before after onlyParents?] :as param} (pco/params env)
        params (cond before {:before before :last 10}
                     after {:after after :first 10}
                     :else {:first 10})

        params (merge params
                      (when onlyParents? {:filter {:children {:length {:gt 0.0}}}}))]
    (let [req (-> (linear-req {:queries
                               [[:issues params
                                 [[:nodes [:id
                                           :title
                                           :description
                                           :createdAt
                                           :priorityLabel
                                           :estimate
                                           :identifier
                                           [:children [[:nodes [:id]]]]]]]]]})
                  :data :issues :nodes wrap-keywords)
          linearid->tagid (->> (exec! (-> (h/select :id :domain_pk)
                                          (h/from :tags)
                                          (h/where :in :domain_pk (map ::id req))
                                          (h/where [:= :domain_pk_namespace "linear.issue"])))
                               (map (juxt :tags/domain_pk :tags/id))
                               (into {}))
          req (if before (reverse req) req)
          req (map #(let [id (::id %)
                          tagid (linearid->tagid id)]
                      (if linearid (assoc % :tags/id tagid) %)) req)]
      {::issues req})))

(comment (->> (issues) ::issues (map ::title)))

(def resolvers [issues start-sorting-issue sorted-issues])



