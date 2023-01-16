(ns ssorter.rank
  (:require [coffi.mem :as mem :refer [defalias]]
            [coffi.ffi :as ffi :refer [defcfn]]))

(defcfn strlen
  "Given a string, measures its length in bytes."
  strlen [::mem/c-string] ::mem/long)

(ffi/load-library "pagerank/pr.so")

(strlen "hello")
;; => 5


