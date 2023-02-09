(ns ssorter.client.ui
  (:require 
   [ssorter.client.mutations :as mut]
   [ssorter.model.integrations.linear :as linear]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
   [com.fulcrologic.fulcro.algorithms.normalized-state :as norm]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
   [com.fulcrologic.fulcro.raw.components :as rc]
   [com.fulcrologic.fulcro.data-fetch :as df]    
   [com.fulcrologic.fulcro.dom :as dom :refer [button div form h1 h2 h3 input label li ol p ul]]
   
   [com.fulcrologic.semantic-ui.factories :as f]))

(defsc Root [this props]
  {:query [[df/marker-table :load-progress]
           :new-thing
           {:root/issues (comp/get-query linear/IssueList)}]
   :initial-state {:root/issues {}}}
  
  (->> (f/ui-tab {:menu {:fluid true :vertical true}
                  :panes [{:menuItem "linear/issues"
                           :render
                           (fn [] (comp/with-parent-context this
                                    (linear/ui-issue-list (:root/issues props))))}
                          {:menuItem "sorted: TOM-317" :render (fn [] (f/ui-tab-pane nil "epic2"))}]})
       (f/ui-container nil)
       (f/ui-segment {:style {:padding "8em 0em"} :vertical true})))
