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
                  (m.pairs/ui-pair (:tags/pair props))
                  (ui-sorted (:tags/sorted props))
                  (m.votes/ui-vote-list {:list (:tags/votes props)
                                         :tags/id (:tags/id props)})))

(def ui-tag (comp/factory Tag))

(defsc TagRow [this props]
  {:ident :tags/id
   :query [:tags/id
           :tags/title]}
  (f/ui-table-row {:style {:cursor "pointer"
                           :positive true}}
                  (f/ui-table-cell {:singleLine true
                                    :onClick #(dr/change-route this
                                                               ["tag" (:tags/id props)])} (:tags/title props))))

(def ui-tag-row (comp/factory TagRow {:keyfn :tags/id}))

(defsc TagList [this props]
  {:ident (fn [] [:component/id :TagList])
   :initial-state {:tags []}
   :query [{:tags (comp/get-query TagRow)}]
   :route-segment ["tags"]
   :will-enter (fn [app route-params]
                 (println "this is being run")
                 (dr/route-deferred
                  [:component/id :TagList]
                  #(df/load! app :tags TagList
                             {:post-mutation `dr/target-ready
                              :post-mutation-params {:target [:component/id :TagList]}})))}
  
  (f/ui-container
   {}

   (f/ui-table
    {:celled true :striped true :compact true :selectable true}
    (->> "Issues"
         (f/ui-table-header-cell {:colSpan 100} (f/ui-loader {:active false}))
         (f/ui-table-row nil)
         (f/ui-table-header nil))
    
    (f/ui-table-body nil
                     (concat
                      (->> props
                           :tags
                           (map ui-tag-row))))
    (comment (->>
              (f/ui-menu {:pagination true
                          :size "mini"
                          :fluid true}
                         left-arrow
                         right-arrow)
              (f/ui-table-header-cell {:colSpan 100})
              (f/ui-table-row nil)
              (f/ui-table-footer nil))))))

(defn load [app & [params]]
  ;; TODO do a pre merge filter so i don't merge in empty list..
  (df/load! app :tags Tag
            {:params (or params {})
             :target (targeting/replace-at
                      [:component/id :TagList :tags])
             :marker ::spinner}))
