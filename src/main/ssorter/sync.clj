(ns ssorter.sync
  (:require [clojure.set :as set]
            [tick.core :as t]))

(defn split-by-inst [id1->ts id2->ts]
  (def id1->ts id1->ts)
  (def id2->ts id2->ts)
  (def left-keys (set (keys id1->ts)))
  (def right-keys (set (keys id2->ts)))
  (def both-keys (clojure.set/intersection left-keys right-keys))

  (comment

  ;; 15th, 16th
  ;; older, newer
  ;; less, greater
    
    (t/> (get id1->ts (first both-keys))
         (get id2->ts (first both-keys))))

  {:left-only (clojure.set/difference left-keys right-keys)
   :right-only (clojure.set/difference right-keys left-keys)
   :middle-equal (->> both-keys (filter #(t/= (get id1->ts %)
                                              (get id2->ts %))) set)
   :middle-left-newer (->> both-keys (filter #(t/> (get id1->ts %)
                                                   (get id2->ts %))) set)
   :middle-right-newer (->> both-keys (filter #(t/< (get id1->ts %)
                                                    (get id2->ts %))) set)})

