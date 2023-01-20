(ns ssorter.rank
  (:require [clojure.test :refer [deftest is]]
            [jsonista.core :as j]))


(def mapper
  (j/object-mapper
   {:encode-key-fn name
    :decode-key-fn keyword}))


(declare decode-v)
(defn decode-kv [[left right]]
  (let [decoded-left (decode-v left)
        decoded-right (decode-v right)]
    (if (nil? decoded-left)
      nil
      {(if (string? decoded-left)
         (keyword decoded-left)
         decoded-left)
       decoded-right})))

(defn decode-v [[left right]]
  (case left
    "table" (let [mapped (into {} (map decode-kv right))]
              (if (every? #(number? (key %)) mapped)
                (vec (map second mapped))
                mapped))
    "number" (Integer/parseInt right)
    "string" right
    "boolean" (Boolean/valueOf right)
    "metatable" nil
    "recursion" nil
    "function" nil))

(defn parse-file [fname]
  (-> (slurp fname)
      (j/read-value mapper)
      :lines
      ffirst
      decode-v))

(def items (parse-file "src/test/ssorter/items.json"))
(def votes (parse-file "src/test/ssorter/votes.json"))

(def fruits-id "16c5f113-66cc-4514-8df0-4092a99285c0")
(def mtg-id "fdd74412-92e4-460f-ae80-19d6befef509")

(defn votes-for [tag_id]
  (->> votes
       (filter (comp #{tag_id} :tag_id))))

(defn items-in [votes]
  (distinct (flatten (map (juxt :item_a :item_b) votes))))

(def winners (take 3 (let [votes (votes-for fruits-id)]
                       (sorted (items-in votes) votes))))

(->> items
     (filter (comp (set (map first winners)) :id))
     (map :name))

(deftest pagerank []
  (is (= 3 3)))
