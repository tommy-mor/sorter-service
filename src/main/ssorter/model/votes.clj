(ns ssorter.model.votes
  (:require
   [ssorter.server-components.db :refer [exec!]]
   [taoensso.timbre :as log]
   [honey.sql.helpers :as h]
   [honey.sql :as hq]
   [com.wsscode.pathom3.connect.built-in.resolvers :as pbir]
   [com.wsscode.pathom3.connect.operation :as pco]))

(pco/defresolver votes [env input]
  {:votes (exec! (-> (h/select :id)
                     (h/from :votes)))})

(pco/defresolver vote [env input]
  {::pco/output [{:vote [:votes/id]}]}
  (let [params (pco/params env)]
    {:vote {:votes/id (:votes/id params)}}))

(pco/defresolver vote-fields [env {:keys [:votes/id]}]
  {::pco/output [:votes/attribute
                 :votes/created_at
                 :votes/left_item_id
                 :votes/domain_pk_namespace
                 :votes/id
                 :votes/edited_at
                 :votes/right_item_id
                 :votes/magnitude
                 :votes/access]}
  (first (exec! (-> (h/select :*)
                    (h/from :votes)
                    (h/where [:= :id id])))))

(pco/defresolver vote-fields [env {:keys [:votes/id]}]
  {::pco/output [:votes/attribute
                 :votes/created_at
                 :votes/left_item_id
                 :votes/domain_pk_namespace
                 :votes/id
                 :votes/edited_at
                 :votes/right_item_id
                 :votes/magnitude
                 :votes/access]}
  (first (exec! (-> (h/select :*)
                    (h/from :votes)
                    (h/where [:= :id id])))))

(pco/defresolver left-item [env {:keys [:votes/left_item_id]}]
  {:votes/left_item {:items/id left_item_id}})

(pco/defresolver right-item [env {:keys [:votes/right_item_id]}]
  {:votes/right_item {:items/id right_item_id}})

(pco/defresolver tag-votes [env {:keys [:tags/sorted]}]
  
  {:tags/votes (:sorted/votes sorted)})

(pco/defmutation create [{:votes/keys [left_item_id
                                       right_item_id
                                       magnitude
                                       attribute]
                          :tags/keys [id]
                          :as new-vote}]
  (assert (not= left_item_id right_item_id))
  (def new-vote new-vote)

  "TODO dedupe votes based on ownership, who created them. one user cannot duplicate votes.."
  
  (def r (-> (exec! (-> (h/insert-into :votes)
                        (h/values [(dissoc new-vote :tags/id)])
                        (h/returning :id)))
             first))
  (assoc r :tags/id id))

(pco/defmutation delete [vote]
  (assert (:votes/id vote))
  
  (exec! (-> (h/delete-from :votes)
             (h/where [:= :id (:votes/id vote)])
             (h/returning :id)))
  {})

(def resolvers [votes vote vote-fields left-item right-item

                create delete

                tag-votes])

(comment (comment
           (vote-fields {} {:votes/id 1})))
