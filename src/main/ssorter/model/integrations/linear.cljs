(ns ssorter.model.integrations.linear
  (:require 
   [ssorter.client.mutations :as mut]
   
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
   [com.fulcrologic.fulcro.algorithms.normalized-state :as norm]
   [com.fulcrologic.fulcro.mutations :as m]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
   [com.fulcrologic.fulcro.raw.components :as rc]
   [com.fulcrologic.fulcro.data-fetch :as df]    
   [com.fulcrologic.fulcro.dom :as dom
    :refer
    [button div form h1 h2 h3 input label li ol p ul pre]]
   
   [com.fulcrologic.semantic-ui.factories :as f]
   [clojure.contrib.humanize :refer [datetime]]))

(defsc Issue [this props]
  {:ident ::id
   :query [::id ::title ::createdAt ::priorityLabel ::children ::identifier]
   :initial-state {}}
  (let [opts {:singleLine true}]
    (f/ui-table-row nil
                    (f/ui-table-cell opts (dom/a {:onClick #(js/alert "swag")
                                                  :href "#"} (::identifier props)))
                    (f/ui-table-cell opts (::title props))
                    (f/ui-table-cell opts (pr-str (-> props ::children ::nodes count)))
                    (f/ui-table-cell opts (datetime (::createdAt props)))
                    (f/ui-table-cell opts (::priorityLabel props)))))


(def ui-issue (comp/factory Issue {:keyfn ::id}))


(defn load [app & [params]]
  ;; TODO do a pre merge filter so i don't merge in empty list..
  (df/load! app ::issues Issue
            {:params (assoc params :onlyParents? true)
             :target (targeting/replace-at
                      [:component/id :IssueList ::issues])
             :marker ::spinner}))

(defsc IssueList [this props]
  {:ident (fn []  [:component/id :IssueList])
   :initial-state {::issues []}
   :query [{::issues (comp/get-query Issue)}
           [df/marker-table ::spinner]]}
  (def props props)

  (let [left-arrow
        (f/ui-menu-item {:as "a"
                         :onClick
                         #(load this {:before (-> props ::issues first ::id)})} "<")
        right-arrow
        (f/ui-menu-item {:as "a"
                         :onClick
                         #(load this {:after (-> props ::issues last ::id)})} ">")

        spinner (df/loading? (get props [df/marker-table ::spinner]))]
    (->> (f/ui-table {:celled true :striped true :compact true}
                     (->> (f/ui-breadcrumb {:sections [{:key "issues" :content "issues"}
                                                       {:key "tom-315" :content "tom-315" :link true}]})
                          (f/ui-table-header-cell {:colSpan 100} (f/ui-loader {:active spinner}))
                          (f/ui-table-row nil)
                          (f/ui-table-header nil))
                     (f/ui-table-body nil
                                      (map ui-issue (::issues props)))
                     (->>
                      (f/ui-menu {:pagination true
                                  :size "mini"
                                  :fluid true}
                                 left-arrow
                                 right-arrow)
                      (f/ui-table-header-cell {:colSpan 100})
                      (f/ui-table-row nil)
                      (f/ui-table-footer nil)))
         (f/ui-container nil))))



(def ui-issue-list (comp/factory IssueList))