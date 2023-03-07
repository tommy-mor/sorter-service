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
   [clojure.contrib.humanize :refer [truncate]]
   
   [ssorter.model.integrations.linear-tag :as linear-tag]))

(defn thing [all]
  (loop [out []
         current []]
    (if (= current all)
      out
      (let [letter (first (drop (count current) all))]
        (recur (conj out (conj current letter))
               (conj current letter))))))

(comment
  (thing ["a" "b" "c"]) ;= [["a"] ["a" "b"] ["a" "b" "c"]] 
  )

(defrouter RootRouter [this props]
  {:router-targets [linear/IssueList m.tags/Tag linear-tag/LinearTag]
   :always-render-body? true}
  (def xx props)
  (comp/fragment
   (f/ui-segment {} (f/ui-breadcrumb {}
                                     (let [segments (-> props :router-state :path-segment)]
                                       (when segments
                                         (->> (map (fn [title link]
                                                     (f/ui-breadcrumb-section {:link true
                                                                               :key title
                                                                               :onClick #(dr/change-route! this link)} title))
                                                   segments
                                                   (thing  segments))
                                              (interleave (map #(f/ui-breadcrumb-divider {:key %}) (range)))
                                              (drop 1))))))
   (when (not= :routed (:current-state props))
     (div "pending"))
   (when-let [route-factory (:route-factory props)]
     (route-factory (comp/computed (:route-props props) (comp/get-computed this))))))

(def ui-root-router (comp/factory RootRouter))

(defsc Root [this props]
  {:query [{:root/router (comp/get-query RootRouter)}]
   :initial-state {:root/router {}}}

  (def x props)
  (let [render (fn [] (comp/with-parent-context this
                        (ui-root-router (:root/router props))))
        nav-to (fn [route]
                 "TODO"
                 (dr/change-route! this route))]
    
    ;; TODO dr/change-route onclicks
    (div
     #_(dom/pre (with-out-str (cljs.pprint/pprint props)))
     (let [menu (f/ui-menu {:vertical true}
                           (f/ui-menu-item {:link true
                                            :onClick #(nav-to ["linear.issues"])} "linear/issues")
                           (f/ui-menu-item {:link true
                                            :onClick #(nav-to ["youtube.videos"])} "youtube"))]
       (->> (f/ui-grid {}
                       
                       (f/ui-grid-column {:width 4} menu)
                       (f/ui-grid-column {:width 12
                                          :stretched true} (render)))
            (f/ui-container nil)
            (f/ui-segment {:style {:padding "8em 0em"} :vertical true}))))))
