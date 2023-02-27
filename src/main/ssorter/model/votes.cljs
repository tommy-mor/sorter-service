(ns ssorter.model.votes
  (:require
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
  (remote [env] (returning env 'ssorter.model.tags/Tag)))

(defmutation delete [vote]
  (action [{:keys [state] :as env}]
          (println "sending vote.." (pr-str vote) )
          state)
  (remote [env]
          (def env env)
          (returning env 'ssorter.model.tags/Tag {:query-params {:tags/id 71}})))

(defsc Vote [this props]
  {:ident :votes/id
   :query [:votes/id
           {:votes/left_item [:items/title]}
           {:votes/right_item [:items/title]}
           :votes/magnitude
           :votes/edited_at]}
  (def x props)
  "{{{ NOT SURE HOW TO SOLVE CALLBACK PROBLEM: how to get these mutations to
    refresh the entire tag. it is outside of their scope, so they should get a reload callback controlled by parent...
    callback could be just a function that calls load! (these are merged/debounced? with mutations somehow..)

    https://book.fulcrologic.com/#_mutations_that_trigger_one_or_more_loads


   }}}"

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

(defsc VoteList [this {:keys [list]}]
  {:initLocalState (fn [_ _] {::selected nil})}
  
  (->>
   (for [vote (reverse list)]
     (ui-vote vote))
   (f/ui-table-body {})
   (f/ui-table {:selectable true})))

(def ui-vote-list (comp/factory VoteList))


