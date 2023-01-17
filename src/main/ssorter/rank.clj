(ns ssorter.rank
  (:require [clojure.java.shell :refer [sh]]
            [clojure.java.io :as io]
            [taoensso.timbre :as log]))

(def pr-location (delay (let [loc (java.io.File. "./pr")]
                          (if (.exists loc)
                            loc
                            (let [res (io/file (io/resource "pr"))]
                              (assert (.exists res))
                              
                              (log/info "moving pr executable out of resources and into cwd")
                              
                              (with-open [in (io/input-stream res)
                                          out (io/output-stream loc)]
                                (io/copy in out))
                              
                              (.setExecutable loc true))))))

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


