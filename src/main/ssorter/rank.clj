(ns ssorter.rank
  (:require [clojure.java.shell :refer [sh]]
            [clojure.java.io :as io]
            [taoensso.timbre :as log]
            [clojure.string :as str]))

(def pr-location (delay (let [loc (java.io.File. "./pr")]
                          (if (.exists loc)
                            loc
                            (let [res (io/file (io/resource "pr"))]
                              (assert (.exists res))
                              
                              (log/info "moving pr executable out of resources and into cwd")
                              
                              (with-open [in (io/input-stream res)
                                          out (io/output-stream loc)]
                                (io/copy in out))
                              
                              (.setExecutable loc true)
                              (assert (.exists loc))
                              loc)))))

(defn pagerank-file [fname]
  (->> (:out (sh (str @pr-location)
                 fname))
       (str/split-lines)
       (partition 2)
       (map vec)
       (into {})))


(defn edges->energy [edges]
  "given a lot of edges, put them into graphit and get ranks.
   returns {idx -> float}, where idx is index/position in arg")


(defn votes->edges [items votes]
  (let [item->idx (into {} (map-indexed #(vector %2 %1) items))]
    (for [{:keys [item_a item_b magnitude]} votes]
      [(item->idx item_a) (item->idx item_b) magnitude])))
