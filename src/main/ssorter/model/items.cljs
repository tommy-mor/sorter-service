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
  (def props props)
  (let [opts {:singleLine true}]
    (f/ui-table-row {}
                    (f/ui-table-cell opts (truncate (str (:items/score props)) 5 ""))
                    (f/ui-table-cell opts (truncate (:items/title props) 90)))))

(def ui-item (comp/factory Item {:keyfn :items/id}))

(defsc ItemList [this {:keys [list title]}]
  (if (empty? list)
    (->> (str "this tag has no " title " items")
         (f/ui-table-cell {:singleLine true})
         (f/ui-table-row {:disabled true})
         (f/ui-table-body {})
         (f/ui-table {:attached true}))
    
    (->> (f/ui-table {:attached true}
                     (f/ui-table-body {}
                                      (map ui-item list))))))

(def ui-item-list (comp/factory ItemList))
