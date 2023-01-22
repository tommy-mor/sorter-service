(ns ssorter.rank
  (:require [clojure.test :refer [deftest is]]
            [jsonista.core :as j]))


;; with symmetric
;; with filtering (faster)
;; with commented out
(deftest pagerank []
  (is (= 3 3)))
