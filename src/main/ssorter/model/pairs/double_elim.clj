(ns ssorter.model.pairs.double-elim
  (:require
   [ssorter.server-components.db :refer [exec!]]
   [taoensso.timbre :as log]
   [honey.sql.helpers :as h]
   [com.wsscode.pathom3.connect.built-in.resolvers :as pbir]
   [com.wsscode.pathom3.connect.operation :as pco]))



(defn pair [items votes]
  "give pairs in bracket order. need to draw out.
     properties: constructs a bracket if only this alg is used
     if you beat someone, then you play the other person who beat that person, until you are king or lose.
      do this until everyone has a vote. then switch to something more approximate.

      where to start? 25th percentile (pretty low, should be able to win a couple..)
                 randomly from bottom 75%? then elomatch second one, start climbing.
"
  
  
  )

