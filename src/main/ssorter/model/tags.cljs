(ns ssorter.model.tags
  (:require 
   [ssorter.model.items :as m.items]
   [ssorter.model.pairs :as m.pairs]
   [ssorter.model.votes :as m.votes]
   
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
   [com.fulcrologic.fulcro.mutations :as m]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
   [com.fulcrologic.fulcro.raw.components :as rc]
   [com.fulcrologic.fulcro.data-fetch :as df]
   
   [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
   [com.fulcrologic.fulcro.dom :as dom
    :refer
    [button div form h1 h2 h3 input label li ol p ul pre]]
   
   [com.fulcrologic.semantic-ui.factories :as f]))


(defsc Sorted [this props]
  {:ident :sorted/id
   :query [:sorted/id
           {:sorted/unsorted (comp/get-query m.items/Item)}
           {:sorted/sorted (comp/get-query m.items/Item)}]
   :initLocalState (fn [_ _] {::segment 0})}
  
  (let [segment (comp/get-state this ::segment)
        onclick (fn [idx] #(comp/set-state! this {::segment idx}))]
    (f/ui-accordion {:styled true
                     :fluid true}
                    (f/ui-accordion-title {:active (= 0 segment)
                                           :onClick (onclick 0)}
                                          "sorted")
                    (f/ui-accordion-content {:active (= 0 segment)
                                             :style {:padding 0}}
                                            (m.items/ui-item-list {:list (:sorted/sorted props)
                                                                   :title "sorted"}))
                    
                    (f/ui-accordion-title {:active (= 1 segment)
                                           :onClick (onclick 1) }
                                          (str "unsorted ("
                                               (count (:sorted/unsorted props))
                                               ")"))
                    (f/ui-accordion-content {:active (= 1 segment)
                                             :style {:padding 0}}
                                            (m.items/ui-item-list {:list (:sorted/unsorted props)
                                                                   :title "unsorted"})))))

(def ui-sorted (comp/factory Sorted))

(defsc Tag [this props]
  {:ident :tags/id
   :query [:tags/id
           :tags/title
           :tags/slug
           :tags/domain_pk_namespace
           {:tags/sorted (comp/get-query Sorted)}
           {:tags/pair (comp/get-query m.pairs/Pair)}
           {:tags/votes (comp/get-query m.votes/Vote)}]

   :route-segment ["tag" :tag-id]
   :will-enter (fn [app {:keys [tag-id]}]
                 (when-let [tag-id (some-> tag-id (js/parseInt))]
                   (dr/route-deferred [:tags/id tag-id]
                                      #(df/load! app [:tags/id tag-id] Tag
                                                 {:post-mutation `dr/target-ready
                                                  :post-mutation-params {:target [:tags/id tag-id]}}))))}
  (f/ui-container {}
                  (f/ui-segment {}
                                (f/ui-header {:as "h2"}
                                             (:tags/title props))
                                (:tags/description props)
                                (:tags/slug props))
                  (when (= "linear.issue" (:tags/domain_pk_namespace props))
                    (f/ui-segment {}
                                  (dom/pre {:id "debuglog" :style {:margin 0 }})
                                  (f/ui-button {:fluid true
                                                   :onClick #(transact! this [(ssorter.model.integrations.linear/sync props)])} "sync!")))
                  (ui-sorted (:tags/sorted props))
                  (m.pairs/ui-pair (:tags/pair props))
                  (m.votes/ui-vote-list {:list (:tags/votes props)
                                         :tags/id (:tags/id props)})))

(def ui-tag (comp/factory Tag))

(defsc TagList [this props]
  {:ident (fn [] [:component/id :TagList])
   :initial-state {:tags []}
   :query [{:tags (comp/get-query Tag)}]}
  "this is never used, because we use the data loaded here in ssorter.client.ui")

(defn load [app & [params]]
  ;; TODO do a pre merge filter so i don't merge in empty list..
  (df/load! app :tags Tag
            {:params (or params {})
             :target (targeting/replace-at
                      [:component/id :TagList :tags])
             :marker ::spinner}))
