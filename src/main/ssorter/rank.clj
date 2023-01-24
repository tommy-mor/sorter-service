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

(defn selfnode [edges]
  (let [fname (edges->tmpfile edges)
        lines (str/split-lines (:out (sh (str @self-node-location)
                                         (.getAbsolutePath fname))))
        #_max_degree #_(parse-long (first lines))
        outedges (->> lines
                      (drop 1)
                      (partition 2)
                      (map (fn [[a b]] (let [node (parse-long a)] [node node (parse-long b)]))))]
    (.delete fname)
    outedges))


(defn pagerank [edges]
  (let [fname (edges->tmpfile edges)
        ranks 
        (->> (:out (sh (str @pr-location)
                       (.getAbsolutePath fname)))
             (str/split-lines)
             (partition 2)
             (map (fn [[a b]] [(parse-long a) (parse-float b)]))
             (into {}))]
    (.delete fname)
    ranks))

(defn edges->energy [edges]
  "given a lot of edges, put them into graphit and get ranks.
   returns {idx -> float}, where idx is index/position in arg"
  (let [self-nodes (selfnode edges)]
    self-nodes))


(defn votes->edges [items->idx votes]
  (->> (for [{:keys [item_a item_b magnitude]} votes]
         (list [(items->idx item_a) (items->idx item_b) magnitude]
               [(items->idx item_b) (items->idx item_a) (- magnitude 100)]))
       (apply concat)
       (concat (for [[k idx] items->idx]
                 [idx idx 1]))))

(defn sorted [items votes]
  (let [item->idx (into {} (map-indexed #(vector %2 %1) items))
        edges (votes->edges item->idx votes)
        energies (edges->energy edges)]
    (sort-by second > (for [item items
                            :let [idx (get item->idx item)
                                  score (get energies idx)]]
                        [item score]))))


