(ns ssorter.model.votes
  (:require
   [ssorter.client.util :as util]
   
   [com.fulcrologic.fulcro.mutations :refer [defmutation returning]]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
   [com.fulcrologic.fulcro.dom :as dom
    :refer
    [button div form h1 h2 h3 input label li ol p ul pre]]
   [com.fulcrologic.semantic-ui.factories :as f]
   [clojure.contrib.humanize :refer [datetime truncate]]))

(defmutation create [vote]
  (action [{:keys [state] :as env}]
          (println "sending vote.." (pr-str vote) )
          state)
  (remote [env] (returning env 'ssorter.model.tags/Tag {:query-params {:tags/id (:tags/id vote)}})))

(defmutation delete [vote]
  (action [{:keys [state] :as env}]
          (println "sending vote.." (pr-str vote) )
          state)
  (remote [env]
          (returning env 'ssorter.model.tags/Tag {:query-params {:tags/id (:tags/id vote)}})))

(defsc Vote [this props]
  {:ident :votes/id
   :query [:votes/id
           {:votes/left_item [:items/title]}
           {:votes/right_item [:items/title]}
           :votes/magnitude
           :votes/edited_at]}
  (f/ui-table-row {:selected (::selected props)}
                  (f/ui-table-cell {} (truncate (-> props :votes/left_item :items/title) 30))
                  (f/ui-table-cell {} (:votes/magnitude props))
                  (f/ui-table-cell {} (truncate (-> props :votes/right_item :items/title) 30))
                  (f/ui-table-cell {} (datetime (:votes/edited_at props)))
                  (f/ui-table-cell {:onClick #(transact! this [(delete props)])}
                                   (f/ui-button {:basic true
                                                 :icon "delete"
                                                 :color "red"}))))

(def ui-vote (comp/factory Vote {:keyfn :votes/id}))

(defsc VoteList [this {:keys [list] :as props}]
  {:initLocalState (fn [_ _] {::expanded? false})}
  
  (let [expanded? (comp/get-state this ::expanded?)]
    (->>
     (concat (for [vote (if expanded?
                          (reverse list)
                          (take 3 (reverse list)))]
               (ui-vote (if-let [tagid (:tags/id props)]
                          (assoc vote :tags/id tagid)
                          vote)))
             (if (and (not expanded?) (> (count list) 3))
               [(util/expand-row {:title (str "click here to show " (- (count list) 3) " more votes")
                                  :onClick #(comp/set-state! this {::expanded? true})})]
               []))
     (f/ui-table-body {})
     (f/ui-table {:selectable true}))))

(def ui-vote-list (comp/factory VoteList))


