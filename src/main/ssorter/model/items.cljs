(ns ssorter.model.items
  (:require 
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
   [com.fulcrologic.fulcro.mutations :as m]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
   [com.fulcrologic.fulcro.raw.components :as rc]
   [com.fulcrologic.fulcro.data-fetch :as df]    
   [com.fulcrologic.fulcro.dom :as dom
    :refer
    [button div form h1 h2 h3 input label li ol p ul pre]]
   
   [com.fulcrologic.semantic-ui.factories :as f]
   
   [clojure.contrib.humanize :refer [truncate]]))

(defsc BigItem [this props]
  {:ident :items/id
   :query [:items/id :items/title]}
  (f/ui-segment {} (h3 (:items/title props))))

(def ui-big-item (comp/factory BigItem {:keyfn :items/id}))

(defsc Item [this props]
  {:ident :items/id
   :query [:items/id :items/title :items/score]}
  (let [opts {:singleLine true}]
    (f/ui-table-row {}
                    (f/ui-table-cell opts (truncate (str (:items/score props)) 5 ""))
                    (f/ui-table-cell opts (truncate (:items/title props) 90)))))

(def ui-item (comp/factory Item {:keyfn :items/id}))

(defsc ItemList [this {:keys [list title]}]
  (let [llist list]
    (def llist llist)
    (if (empty? llist)
      (->> (str "this tag has no " title " items")
           (f/ui-table-cell {:singleLine true})
           (f/ui-table-row {:disabled true})
           (f/ui-table-body {})
           (f/ui-table {:attached true}))

      (->> (f/ui-table {:attached true}
                       (f/ui-table-body {}
                                        (if (every? :items/score llist)
                                          (drop-last 1 (interleave (map ui-item llist)
                                                                   (map #(let [score1 (:items/score %1)
                                                                               score2 (:items/score %2)
                                                                               pixels (* 100 (- score1 score2))]
                                                                           (println pixels)
                                                                           (dom/div {:style {:backgroundColor "lightgray"
                                                                                             :height (str pixels "px")}
                                                                                     :key (str "spacer/" (:items/id %1))}))
                                                                        llist
                                                                        (cycle (drop 1 llist)))))
                                          (map ui-item llist))))))))

(def ui-item-list (comp/factory ItemList))
