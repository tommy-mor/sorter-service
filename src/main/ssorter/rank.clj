(ns ssorter.rank
  (:require [clojure.java.shell :refer [sh]]
            [clojure.java.io :as io]
            [taoensso.timbre :as log]
            [clojure.string :as str]))

(defn confirm-location [fname]
  (delay (let [loc (java.io.File. fname)]
           (if (.exists loc)
             loc
             (let [res (io/file (io/resource fname))]
               (assert (.exists res))
               
               (log/info (format "moving file %s executable out of resources and into cwd" fname))
               
               (with-open [in (io/input-stream res)
                           out (io/output-stream loc)]
                 (io/copy in out))
               
               (.setExecutable loc true)
               (assert (.exists loc))
               loc)))))

(def pr-location (confirm-location "./pr"))
(def self-node-location (confirm-location "./self_nodes"))

(defn parse-float [d]
  (case d
    "-nan" Double/NaN
    (Double/parseDouble d)))

(defn dedup-sum
  "sums edges that are between the same node @PERFORMANCE replace with something not in clojure"
  [edges]
  (->> edges
       (group-by (juxt first second))
       (reduce-kv (fn [m k v]
                    (conj m (conj k (apply + (map last v))))) [])))

(defn edges->tmpfile [edges]
  (let [edges (dedup-sum edges)
        tmpfile (java.io.File/createTempFile "edges" ".wel")]
    (with-open [file (io/writer tmpfile)]
      (binding [*out* file]
        (doseq [[leftid rightid weight] edges]
          (print leftid)
          (print " ")
          (print rightid)
          (print " ")
          (println weight))))
    tmpfile))

(defn outdegrees-as-edges [edges]
  (let [fname (edges->tmpfile edges)
        lines (str/split-lines (:out (sh (str @self-node-location)
                                         (.getAbsolutePath fname))))
        #_max_degree #_(parse-long (first lines))
        outedges (->> lines
                      (drop 1)
                      (partition 2)
                      (map (fn [[a b]] (let [node (parse-long a)]
                                         [node (case node 0 1 0) (parse-long b)]))))]
    (.delete fname)
    outedges))


(defn pagerank [edges]
  (let [selfedgesfname (edges->tmpfile (outdegrees-as-edges edges))
        fname (edges->tmpfile edges)
        cmdout (:out (sh (str @pr-location)
                         (.getAbsolutePath fname)
                         (.getAbsolutePath selfedgesfname)))
        _ (comment (prn cmdout))
        ranks
        (->> cmdout
             (str/split-lines)
             (partition 2)
             (map (fn [[a b]] [(parse-long a) (parse-float b)]))
             (into {}))]
    (.delete selfedgesfname)
    (.delete fname)
    ranks))

(defn votes->edges [votes]
  (->> (for [{:votes/keys [left_item_id right_item_id magnitude]} votes]
         (list [left_item_id right_item_id magnitude]
               [right_item_id left_item_id (- 100 magnitude)]))
       (apply concat)))

(defn sorted [items votes]
  (def items items)
  (def votes votes)
  (let [edges (votes->edges votes)
        _ (def edges edges)
        
        energies (pagerank edges)]
    (sort-by second > (for [item items
                            :let [idx (:items/id item)
                                  score (get energies idx)]]
                        [item score]))))


