(ns ssorter.client.ui
  (:require 
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
   [com.fulcrologic.fulcro.routing.dynamic-routing :as dr :refer [defrouter]] 

   
   [com.fulcrologic.semantic-ui.factories :as f]
   [clojure.contrib.humanize :refer [truncate]]))

(defrouter RootRouter [this props]
  {:router-targets [linear/IssueList]}
  (case (:current-state props)
    :pending (div "pending")
    :failed (div "failed")
    
    
    (div "unknown route")))

(def ui-root-router (comp/factory RootRouter))

(defsc Root [this props]
  {:query [{:root/router (comp/get-query RootRouter)}]
   :initial-state {:root/router {}}}

  (let [render (fn [] (comp/with-parent-context this
                        (ui-root-router (:root/router props))))]
    
    ;; TODO dr/change-route onclicks
    (div
     #_(dom/pre (with-out-str (cljs.pprint/pprint props)))
     (->> (f/ui-tab {:menu {:fluid true :vertical true}
                     :panes [{:menuItem "linear/issues"
                              :render render}
                             {:menuItem "youtube/playlists"
                              :render render}
                             {:menuItem "reddit/comments"
                              :render render}]})
          (f/ui-container nil)
          (f/ui-segment {:style {:padding "8em 0em"} :vertical true})))))
