(ns ssorter.client.ui
  (:require 
   [ssorter.client.mutations :as mut]
   [ssorter.model.integrations.linear :as linear]
   [ssorter.model.tags :as m.tags]
   
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
   [com.fulcrologic.fulcro.algorithms.normalized-state :as norm]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
   [com.fulcrologic.fulcro.raw.components :as rc]
   [com.fulcrologic.fulcro.data-fetch :as df]    
   [com.fulcrologic.fulcro.dom :as dom :refer [button div form h1 h2 h3 input label li ol p ul]]
   
   [com.fulcrologic.semantic-ui.factories :as f]
   [clojure.contrib.humanize :refer [truncate]]))

(defn panes-from-tags [this tags]
  (vec (for [tag tags]
         {:menuItem
          (-> tag :tags/title (truncate 50))
          :render (fn []
                    (comp/with-parent-context this
                      (m.tags/ui-tag tag)))})))

(defsc Root [this props]
  {:query [{:root/issues (comp/get-query linear/IssueList)}
           {:root/tags (comp/get-query m.tags/TagList)}]
   :initial-state {:root/issues {}
                   :root/tags {}}}
  
  (div
   #_(dom/pre (with-out-str (cljs.pprint/pprint props)))
   (->> (f/ui-tab {:menu {:fluid true :vertical true}
                   :panes (concat [{:menuItem "linear/issues"
                                    :render
                                    (fn [] (comp/with-parent-context this
                                             (linear/ui-issue-list (:root/issues props))))}]
                                  (panes-from-tags this (-> props :root/tags :tags)))})
        (f/ui-container nil)
        (f/ui-segment {:style {:padding "8em 0em"} :vertical true}))))
