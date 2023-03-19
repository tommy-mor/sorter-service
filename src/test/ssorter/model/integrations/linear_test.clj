(ns ssorter.model.integrations.linear-test
  (:require
   [clojure.test :refer [deftest is]]
   [ssorter.model.integrations.linear :as sut]
   [ssorter.model.tags :as m.tags]
   [ssorter.model.membership :as m.membership]
   [clojure.string :as str]
   [mount.core :as mount]
   [ssorter.server-components.pathom :refer [build-parser]]))

(defn get-special-issue []
  (->> (sut/linear-req {:queries
                        [[:issues
                           {:filter {:title {:contains "ssorter.model.integrations.linear-test"}}}
                           [[:nodes [:id :updatedAt]]]]]})
       :data :issues :nodes first :id))

(defn summarize-items [items]
  (->> items
       (map :items/title)
       (map (comp last #(str/split % #" ")))
       sort
       (apply str)))

(deftest full-flow
  (mount/start)
  "TODO do archive/status syncing..."
  (map m.tags/tag (:tags (m.tags/tags)))
  (def parser (build-parser))
  (def tagid (->> (parser {} {} [{:tags [:tags/id :tags/title]} ])
                  :tags
                  (filter #(re-find #"ssorter.model.integrations.linear-test" (:tags/title %)))
                  first
                  :tags/id))
  (when tagid
    (m.tags/delete {:tags/id tagid ::m.tags/cascade? true}))
  
  (def linear-id (get-special-issue))
  (def mutation-response (parser {} {} `[(sut/start-sorting-issue {::sut/id ~linear-id}) ]))
  
  (def tagid (-> mutation-response
                 (get `sut/start-sorting-issue)
                 :tags/id))

  (is (str/includes? (-> (m.tags/tag {:tags/id tagid})
                         :tags/title)
                     
                     (namespace ::test)))

  (def members (parser {} {:tags/id tagid} [:tags/title {:tags/members [:items/title :items/domain_pk]}]))
  (is (= "abcxyz"
         (summarize-items (:tags/members members))))
  
  (sut/sync-linear-parent-with-tag {:tags/id tagid ::sut/id linear-id})
  
  (is (= "abcxyz"
         (summarize-items (:tags/members members))))
  
  (is (= members
         (parser {} {:tags/id tagid} [:tags/title {:tags/members [:items/title :items/domain_pk]}])))

  "editing..."

  (def r (-> (sut/linear-req {:operation {:operation/type :mutation
                                          :operation/name "deleteForIntegrationTest"}
                              :queries [[:issueArchive
                                         {:id
                                          (-> members :tags/members first :items/domain_pk)}
                                         [:success]]]})
             :data :issueArchive :success))

  (is r)
  
  (def x (sut/sync-linear-parent-with-tag {:tags/id tagid ::sut/id linear-id}))
  
  (is (= "bcxyz" (->> (parser {} {:tags/id tagid} [{:tags/members [:items/title :items/domain_pk]}])
                      :tags/members
                      summarize-items)))
  
  (def r (-> (sut/linear-req {:operation {:operation/type :mutation
                                          :operation/name "deleteForIntegrationTest"}
                              :queries [[:issueUnarchive
                                         {:id
                                          (-> members :tags/members first :items/domain_pk)}
                                         [:success]]]})
             :data :issueUnarchive :success))
  (is r)
  
  (def x (sut/sync-linear-parent-with-tag {:tags/id tagid ::sut/id linear-id}))
  
  (is (= "abcxyz" (->> (parser {} {:tags/id tagid} [{:tags/members [:items/title :items/domain_pk]}])
                       :tags/members
                       summarize-items)))
  
  (-> (sut/linear-req {:operation {:operation/type :mutation
                                   :operation/name "editforintegrationtest"}
                       :queries [[:issueUpdate
                                  {:id
                                   (-> members :tags/members first :items/domain_pk)
                                   :input {:title "A"}}
                                  [:success]]]})
      :data :issueUpdate :success)

  (sut/sync-linear-parent-with-tag {:tags/id tagid ::sut/id linear-id})
  
  (is (= "Abcxyz" (->> (parser {} {:tags/id tagid} [{:tags/members [:items/title :items/domain_pk]}])
                       :tags/members
                       summarize-items)))
  
  (-> (sut/linear-req {:operation {:operation/type :mutation
                                   :operation/name "editforintegrationtest"}
                       :queries [[:issueUpdate
                                  {:id
                                   (-> members :tags/members first :items/domain_pk)
                                   :input {:title "a"}}
                                  [:success]]]})
      :data :issueUpdate :success)
  
  (sut/sync-linear-parent-with-tag {:tags/id tagid ::sut/id linear-id})
  
  (is (= "abcxyz" (->> (parser {} {:tags/id tagid} [{:tags/members [:items/title :items/domain_pk]}])
                       :tags/members
                       summarize-items)))
  (mount/stop))

"TODO do test of tag garbage cleanup, when theres an item in multiple tags"
(comment ((create {:tags/title "another"
                   :tags/description "epic"})
          
          (exec! (-> (h/insert-into :items_in_tags)
                     (h/values (for [itemid (->> (m.membership/tag-members tag)
                                                 :tags/members
                                                 (map :items/id)
                                                 (take 2))]
                                 {:memberships/tag_id 7
                                  :memberships/item_id itemid}))))))





