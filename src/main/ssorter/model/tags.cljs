(ns ssorter.model.tags
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

(defsc Sorted [this props]
  {:ident :sorted/id
   :query [:sorted/id
           :sorted/unsorted
           :sorted/sorted]
   :initLocalState (fn [_ _] {::segment 0})}
  
  (let [segment (comp/get-state this ::segment)
        onclick (fn [idx] #(comp/set-state! this {::segment idx}))]
    (f/ui-accordion {:styled true
                     :fluid true}
                    (f/ui-accordion-title {:active (= 0 segment)
                                           :onClick (onclick 0)}
                                          "sorted")
                    (f/ui-accordion-content {:active (= 0 segment)}
                                            (for [item (:sorted/sorted props)]
                                              (div (pr-str item))))
                    
                    (f/ui-accordion-title {:active (= 1 segment)
                                           :onClick (onclick 1) }
                                          "unsorted")
                    (f/ui-accordion-content {:active (= 1 segment)}
                                            (for [item (:sorted/unsorted props)]
                                              (div (pr-str item)))))))

(def ui-sorted (comp/factory Sorted))

(defsc Tag [this props]
  {:ident :tags/id
   :query [:tags/id :tags/title :tags/slug {:tags/sorted (comp/get-query Sorted)}]}
  (f/ui-container {}
                  (f/ui-segment {}
                                (f/ui-header {:as "h2"}
                                             (:tags/title props))
                                (:tags/description props)
                                (:tags/slug props))
                  (ui-sorted (:tags/sorted props))
                  (pr-str props)))

(def ui-tag (comp/factory Tag))

(defsc TagList [this props]
  {:ident (fn [] [:component/id :TagList])
   :initial-state {:tags []}
   :query [{:tags (comp/get-query Tag)}]}
  "this is never used, because we use the data loaded here in ssorter.client.ui")

(defn load [app & [params]]
  ;; TODO do a pre merge filter so i don't merge in empty list..
  (df/load! app :tags Tag
            {:params (or params {})
             :target (targeting/replace-at
                      [:component/id :TagList :tags])
             :marker ::spinner}))
