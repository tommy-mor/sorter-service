(ns ssorter.model.votes
  (:require
   [com.fulcrologic.fulcro.mutations :refer [defmutation returning]]))

(defmutation create [vote]
  (action [{:keys [state] :as env}]
          (println "sending vote.." (pr-str vote) )
          state)
  (remote [env] (returning env ssorter.model.tags/Tag)))


