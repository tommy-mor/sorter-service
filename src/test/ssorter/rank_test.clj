(ns ssorter.rank-test
  (:require [clojure.test :refer [deftest is]]
            [ssorter.rank :as sut]
            [clojure.java.io :as io]
            [clojure.edn]))


(def edges [[0 1 1]
            [0 2 0]
            [0 3 0]
            
            [1 1 1]
            [1 3 4]
            [1 3 4]
            [1 3 4]
            [1 3 4]
            
            [1 2 10]
            [1 3 10]
            [1 4 10]
            [2 1 3]
            [2 3 3]
            [2 4 3]
            [3 1 1]
            [3 2 1]
            [3 4 1]
            [4 1 1]])

(deftest dedup-sum
  (is (= (sut/dedup-sum [[0 1 1]])
         [[0 1 1]]))
  (is (= (sut/dedup-sum [[0 1 1] [0 2 3]])
         [[0 1 1] [0 2 3]]))
  
  (is (= (sut/dedup-sum [[0 2 1] [0 2 3]])
         [[0 2 4]]))
  
  (is (= (sut/dedup-sum [[0 2 1] [4 5 10] [0 2 100]])
         [[0 2 101] [4 5 10]])))

(deftest selfnode
  (is (= (sut/outdegrees-as-edges [[0 1 1]
                                   [0 1 1]
                                   [0 2 20]
                                   [1 0 100]
                                   [2 0 3]])
         [[0 1 22]
          [1 0 100]
          [2 0 3]])))

(deftest pagerank
  (is (= [1 2 0 4 3]
         (map first (sort-by second (sut/pagerank edges)))))
  
  (is (= 1
         (first (last (sort-by second (sut/pagerank [[0 1 1]
                                                     [2 1 1]
                                                     [3 1 1]])))))))

(deftest sorted
  (let [fruits (-> "templates/fruits.edn" io/resource slurp clojure.edn/read-string)
        fruit-votes (-> "templates/fruit-votes.edn" io/resource slurp clojure.edn/read-string)]
    (is (= ["Watermelon" "Grapes" "Banana"]
           (->> (sut/sorted fruits fruit-votes)
                (map (comp :items/title first))
                (take 3))))))

