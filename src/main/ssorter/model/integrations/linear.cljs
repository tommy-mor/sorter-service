(ns ssorter.model.integrations.linear
  (:require 
   [ssorter.client.mutations :as mut]
   
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
   [com.fulcrologic.fulcro.algorithms.normalized-state :as norm]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
   [com.fulcrologic.fulcro.raw.components :as rc]
   [com.fulcrologic.fulcro.data-fetch :as df]    
   [com.fulcrologic.fulcro.dom :as dom
    :refer
    [button div form h1 h2 h3 input label li ol p ul pre]]
   
   [com.fulcrologic.semantic-ui.factories :as f]))

(defsc Issue [this props]
  {:ident ::id
   :query [::id ::title ::createdAt]
   :initial-state {}}
  (f/ui-table-row nil
                  (f/ui-table-cell nil (::title props))
                  (f/ui-table-cell nil (::createdAt props))))


(def ui-issue (comp/factory Issue {:keyfn ::id}))

(defsc IssueList [this props]
  {:ident (fn []  [:component/id :IssueList])
   :initial-state {::issues []}
   :query [{::issues (comp/get-query Issue)}]}
  (->> (f/ui-table {:celled true :striped true :compact true}
                   (->> (f/ui-breadcrumb {:sections [{:key "issues" :content "issues"}
                                                     {:key "tom-315" :content "tom-315" :link true}]})
                        (f/ui-table-header-cell {:colSpan 3})
                        (f/ui-table-row nil)
                        (f/ui-table-header nil))
                   (f/ui-table-body nil
                                    (map ui-issue (::issues props)))
                   (->>
                    (f/ui-menu {:pagination true
                                :size "mini"
                                :fluid true}
                               (f/ui-menu-item {:as "a"} "1")
                               (f/ui-menu-item {:as "a" :active true} "2")
                               (f/ui-menu-item {:as "a"} "3")
                               (f/ui-menu-item {:as "a"} "4"))
                    (f/ui-table-header-cell {:colSpan 3})
                    (f/ui-table-row nil)
                    (f/ui-table-footer nil)))
       (f/ui-container nil)))



(def ui-issue-list (comp/factory IssueList))
