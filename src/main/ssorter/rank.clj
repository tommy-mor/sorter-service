(ns ssorter.rank
  (:require [clojure.java.shell :refer [sh]]))

(def pr-location )


(sh "")

(defcfn strlen
  "Given a string, measures its length in bytes."
  strlen [::mem/c-string] ::mem/long)

(ffi/load-library "pagerank/pr.so")

(ffi/find-symbol "pagerank_delta_function")

(ffi/reify-libspec {:pg "pagerank/pr.so"})

(defcfn pagerank_delta
  "Given a string, measures its length in bytes."
  pagerank_delta [] ::mem/long)

(strlen "hello")
;; => 5


