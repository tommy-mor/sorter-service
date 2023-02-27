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
   [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
   [com.fulcrologic.fulcro.data-fetch :as df]    
   [com.fulcrologic.fulcro.dom :as dom
    :refer
    [button div form h1 h2 h3 input label li ol p ul pre]]
   
   [com.fulcrologic.semantic-ui.factories :as f]
   [clojure.contrib.humanize :refer [datetime]]))

(m/defmutation start-sorting-issue [params]
  (action [_] (println "running.."))
  (remote [env] true))

(defsc SortedIssue [this props]
  {:ident ::id
   :query [::id :tags/id :tags/title :tags/edited_at]
   :initial-state {}}
  (let [opts {:singleLine true}
        onclick
        #(if (js/confirm "sort this issue?")
           (transact! this [(start-sorting-issue {::id (::id props)})]))]
    (f/ui-table-row {:style {:cursor "pointer"}
                     :positive true}
                    (f/ui-table-cell opts "3 votes")
                    (f/ui-table-cell opts (:tags/title props))
                    (f/ui-table-cell opts nil)
                    (f/ui-table-cell opts (datetime (:tags/edited_at props)))
                    (f/ui-table-cell opts (dom/a {:onClick onclick :href "#"} (::identifier props))))))

(def ui-sorted-issue (comp/factory SortedIssue {:keyfn ::id}))

(defsc Issue [this props]
  {:ident ::id
   :query [::id ::title ::createdAt ::priorityLabel ::children ::identifier]
   :initial-state {}}
  (let [opts {:singleLine true}
        onclick
        #(if (js/confirm "sort this issue?")
           (transact! this [(start-sorting-issue {::id (::id props)})]))]
    (f/ui-table-row nil
                    (f/ui-table-cell opts (dom/a {:onClick onclick :href "#"} (::identifier props)))
                    (f/ui-table-cell opts (::title props))
                    (f/ui-table-cell opts (pr-str (-> props ::children ::nodes count)))
                    (f/ui-table-cell opts (datetime (::createdAt props)))
                    (f/ui-table-cell opts (::priorityLabel props)))))


(def ui-issue (comp/factory Issue {:keyfn ::id}))

(defn load-unsorted-issues! [app & [params]]
  (df/load! app ::issues Issue
            {:params (assoc params :onlyParents? true)
             :target (targeting/replace-at
                      [:component/id :IssueList ::issues])
             :marker ::spinner}))

(m/defmutation page-turn [params]
  (action [env]
          (def x env)
          (def params params)
          (-> params keys)
          (let [comp (:component env)
                {::keys [page page->ids]} (comp/get-state comp)
                data (-> x :state deref :component/id :IssueList)
                
                
                newpage (case (:dir params) :left (dec page) :right (inc page))
                cached (page->ids newpage)
                newdata (if (nil? cached) {::sorted-issues [] ::issues []} cached)]
            
            (when (empty? (::issues newdata))
              (case (:dir params)
                :left (load-unsorted-issues! comp {:before (-> params ::issues first ::id)})
                :right (load-unsorted-issues! comp {:after (-> params ::issues last ::id)})))
            
            (comp/set-state! comp
                             {::page newpage ::page->ids (assoc page->ids page data)})

            (swap! (:state x) #(-> %
                                   (assoc-in [:component/id :IssueList ] newdata))))))

(comment (f/ui-breadcrumb {:sections [{:key "issues" :content "issues"}
                                      {:key "tom-315" :content "tom-315" :link true}]}))

(m/defmutation load-more [params]
  (action [env]
          (load-unsorted-issues! (:app env))
          (:state env)))

(defsc IssueList [this props]
  {:ident (fn []  [:component/id :IssueList])
   :initial-state {::issues []
                   ::sorted-issues []}
   :query [{::issues (comp/get-query Issue)}
           {::sorted-issues (comp/get-query SortedIssue)}
           [df/marker-table ::spinner]]
   
   :initLocalState (fn [_ _] {::page 0
                              ::page->ids {}})

   ;; routing
   :route-segment ["linear.issues"]
   :will-enter (fn [app route-params]
                 (df/load! app ::sorted-issues SortedIssue
                           {:target (targeting/replace-at
                                     [:component/id :IssueList ::sorted-issues])
                            :marker ::spinner})
                 (dr/route-immediate [:component/id :IssueList]))}
  
  (def props props)
  (let [sorted-ids (->> props ::sorted-issues (map ::id) set)
        page (comp/get-state this ::page)
        
        left-arrow
        (f/ui-menu-item {:as "a"
                         :disabled (= page 0)
                         :onClick #(transact! this [(page-turn {:dir :left
                                                                ::issues (::issues props)})])} "<")
        right-arrow
        (f/ui-menu-item {:as "a"
                         :onClick #(transact! this [(page-turn {:dir :right
                                                                ::issues (::issues props)})])} ">")

        spinner (df/loading? (get props [df/marker-table ::spinner]))]
    (->> (f/ui-table {:celled true :striped true :compact true :selectable true}
                     (->> "Issues"
                          (f/ui-table-header-cell {:colSpan 100} (f/ui-loader {:active spinner}))
                          (f/ui-table-row nil)
                          (f/ui-table-header nil))
                     
                     (f/ui-table-body nil
                                      (concat
                                       (when (::sorted-issues props)
                                         (map ui-sorted-issue (::sorted-issues props)))
                                       
                                       (->> props
                                            ::issues
                                            (filter (comp not sorted-ids ::id))
                                            (map ui-issue))
                                       [
                                        (when (and (-> props ::issues empty?)
                                                   (not spinner))
                                            (f/ui-table-row {:key :load-more
                                                             :style {:cursor "pointer"}
                                                             :onClick #(transact! this [(load-more {})])}
                                                            (f/ui-table-cell {:icon
                                                                              (f/ui-icon {:fitted true
                                                                                          :name "arrow circle down"} )
                                                                              :width 4})
                                                            (f/ui-table-cell {}
                                                                             (div "load more issues from linear"))
                                                            (f/ui-table-cell {})
                                                            (f/ui-table-cell {})
                                                            (f/ui-table-cell {})))
                                        
                                        ]))
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
