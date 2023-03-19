(ns build
  (:refer-clojure :exclude [test])
  (:require [org.corfield.build :as bb]))

(def version "0.1.0-SNAPSHOT")
(def main 'ssorter.server-main)


(println "building")

(defn ci "Run the CI pipeline of tests (and build the uberjar)." [opts]
  (-> opts
      (assoc :version version :main main :uber-file "target/app.jar")
      (bb/clean)
      (bb/uber)))
