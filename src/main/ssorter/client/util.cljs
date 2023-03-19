(ns ssorter.client.util
  (:require [com.fulcrologic.fulcro.dom :as dom
             :refer
             [button div form h1 h2 h3 input label li ol p ul pre]]
            
            [com.fulcrologic.semantic-ui.factories :as f]))

(defn expand-row [{:keys [title onClick]}]
  (f/ui-table-row {:key :load-more
                   :style {:cursor "pointer"}
                   :onClick onClick}
                  (f/ui-table-cell {:icon
                                    (f/ui-icon {:fitted true
                                                :name "arrow circle down"} )
                                    :width 4})
                  (f/ui-table-cell {}
                                   (div title))
                  (f/ui-table-cell {})
                  (f/ui-table-cell {})
                  (f/ui-table-cell {})))
