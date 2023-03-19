(ns ssorter.model.pairs.double-elim-test
  (:require
   [clojure.test :refer [deftest is]]
   [ssorter.model.pairs.double-elim :as sut]
   [ssorter.model.tags :as m.tags]
   [ssorter.model.membership :as m.membership]
   [clojure.string :as str]
   [mount.core :as mount]
   [ssorter.server-components.pathom :refer [build-parser]]))


(def items {1 "item a"
            2 "item b"
            3 "item c"})

(deftest double-elim
  (is (= 3 3)))




(comment
  (sut/testfn 33))
