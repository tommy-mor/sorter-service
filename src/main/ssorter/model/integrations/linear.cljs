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
    [button div form h1 h2 h3 input label li ol p ul pre]]))

(defsc Issue [this props]
  {:ident ::id
   :query [::id ::title]}
  (div
   (p "linear issue")
   (pre (pr-str props))))

(def ui-issue (comp/factory Issue))

(defsc IssueList [this props]
  {:ident (fn [] [:component/id :IssueList])
   :query [{:linear/issue-list (comp/get-query Issue)}]}
  (div
   (p "hello from linear")
   (map ui-issue (:linear/issue-list props))))



(def ui-issue-list (comp/factory IssueList))
