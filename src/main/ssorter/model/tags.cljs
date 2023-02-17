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

(defsc Tag [this props]
  {:ident :tags/id
   :query [:tags/id :tags/title :tags/slug]
   :initial-state {}}
  (f/ui-container {}
                  (f/ui-segment {}
                                (f/ui-header {:as "h2"}
                                             (:tags/title props))
                                (:tags/description props))

                  (div "accordion?")))

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
