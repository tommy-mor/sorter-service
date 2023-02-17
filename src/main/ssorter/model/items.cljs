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
   
   [com.fulcrologic.semantic-ui.factories :as f]))

(defsc Item [this props]
  {:ident :items/id
   :query [:items/id :items/title]}
  (h1 (:items/title props)))

(def ui-item (comp/factory Item))

(defsc ItemList [this props]
  (div "list"
       (map ui-item props)
       (dom/pre (pr-str props))))

(def ui-item-list (comp/factory ItemList))
