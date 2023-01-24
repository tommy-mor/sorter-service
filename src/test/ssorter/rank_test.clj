(ns ssorter.rank-test
  (:require [clojure.test :refer [deftest is]]
            [jsonista.core :as j]
            [ssorter.rank :as sut]))


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
            [3 4 1]])

(sort (sut/dedup-sum edges))

(sut/edges->energy (sut/dedup-sum edges))
;; with symmetric
;; with filtering (faster)
;; with commented out
(deftest pagerank []
  (is (= 3 3)))
