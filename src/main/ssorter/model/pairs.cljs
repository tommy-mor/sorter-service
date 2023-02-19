(ns ssorter.model.pairs
  (:require 
   [ssorter.model.items :as m.items]
   [ssorter.model.votes :as m.votes]
   
   [com.fulcrologic.fulcro.mutations :refer [defmutation]]
   
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
   [com.fulcrologic.fulcro.mutations :as m]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
   [com.fulcrologic.fulcro.raw.components :as rc]
   [com.fulcrologic.fulcro.data-fetch :as df]    
   [com.fulcrologic.fulcro.dom :as dom
    :refer
    [button div form h1 h2 h3 input label li ol p ul pre]]
   
   [com.fulcrologic.semantic-ui.factories :as f]))

"TODO do more complex design with sliding thats in my ipad as drawing"

"TODO check mark that controlls if you get a new pair. this way you can try out many combos and see effect"


"{{{{{{{{{finish vote transaction.. ponder if i should merge pair and vote namespaces.. they are same concept? or different. i feel like different.}}}}}}}}}"

(defsc Pair [this props]
  {:ident :sorted/id
   :query [:sorted/id
           {:pair/left (comp/get-query m.items/BigItem)}
           {:pair/right (comp/get-query m.items/BigItem)}]}
  (f/ui-segment  {:style {:position "relative"}}
                 (f/ui-grid {:columns 2}
                            (f/ui-grid-column {}
                                              (m.items/ui-big-item (:pair/left props)))
                            (f/ui-grid-column {}
                                              (m.items/ui-big-item (:pair/right props))))
                 (f/ui-divider {})
                 (let [onclick (fn [mag]
                                 #(transact! this [(m.votes/create
                                                    {:votes/left_item_id
                                                     (-> props :pair/left :items/id)
                                                     :votes/right_item_id
                                                     (-> props :pair/right :items/id)
                                                     :votes/attribute 0
                                                     :votes/magnitude mag
                                                     :tags/id (-> props :sorted/id :tags/id)})]))]
                   (f/ui-button-group {:attached true}
                                      (f/ui-button {:onClick (onclick 10)} ">>>")
                                      (f/ui-button {:onClick (onclick 23)} ">>")
                                      (f/ui-button {:onClick (onclick 37)} ">")
                                      (f/ui-button {:onClick (onclick 50)} "=")
                                      (f/ui-button {:onClick (onclick 63)} "<")
                                      (f/ui-button {:onClick (onclick 77)} "<<")
                                      (f/ui-button {:onClick (onclick 90)} "<<<")))))

(def ui-pair (comp/factory Pair))
