(ns ssorter.model.votes
  (:require
   [com.fulcrologic.fulcro.mutations :refer [defmutation returning]]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
   [com.fulcrologic.fulcro.dom :as dom
    :refer
    [button div form h1 h2 h3 input label li ol p ul pre]]
   [com.fulcrologic.semantic-ui.factories :as f]))

(defmutation create [vote]
  (action [{:keys [state] :as env}]
          (println "sending vote.." (pr-str vote) )
          state)
  (remote [env] (returning env 'ssorter.model.tags/Tag)))

(defsc Vote [this props]
  {:ident :votes/id
   :query [:votes/id :votes/left :votes/right :votes/magnitude]}
  (pr-str props))

(def ui-vote (comp/factory Vote {:keyfn :votes/id}))

(defsc VoteList [this {:keys [list]}]
  (pre "i am a votelist" (pr-str list)))

(def ui-vote-list (comp/factory VoteList))


