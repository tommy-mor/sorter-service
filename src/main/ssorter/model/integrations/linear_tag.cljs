(ns ssorter.model.integrations.linear-tag
  (:require 
   [ssorter.client.util :as util]
   [ssorter.model.integrations.linear :as lin]
   
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
   [com.fulcrologic.fulcro.algorithms.normalized-state :as norm]
   [com.fulcrologic.fulcro.mutations :as m]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
   [com.fulcrologic.fulcro.raw.components :as rc]
   [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
   [com.fulcrologic.fulcro.data-fetch :as df]    
   [com.fulcrologic.fulcro.dom :as dom
    :refer
    [button div form h1 h2 h3 input label li ol p ul pre]]
   
   [com.fulcrologic.semantic-ui.factories :as f]
   [clojure.contrib.humanize :refer [datetime]]))

(defsc LinearTag
  "a tag view f a linear tag that is unsorted/has no votes."
  [this props]
  {:ident ::lin/id
   :query [::lin/id
           ::lin/title]

   :route-segment ["tag.linear.issue" :issue-id]
   :will-enter (fn [app {:keys [issue-id]}]
                 (dr/route-deferred [::lin/id issue-id]
                                    #(df/load! app [::lin/id issue-id]
                                               LinearTag
                                               {:post-mutation `dr/target-ready
                                                :post-mutation-params {:target
                                                                       [::lin/id issue-id]}})))}
  #_ (f/ui-container {}
                     (f/ui-segment {}
                                   (f/ui-header {:as "h2"}
                                                (:tags/title props))
                                   (:tags/description props)
                                   (:tags/slug props))
                     (ui-sorted (:tags/sorted props))
                     (m.pairs/ui-pair (:tags/pair props))
                     (m.votes/ui-vote-list {:list (:tags/votes props)
                                            :tags/id (:tags/id props)}))
  (dom/pre (pr-str props)))



