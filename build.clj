(ns build
  (:require [clojure.tools.build.api :as b]))

(def version (format "0.1.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file "target/app.jar")

(defn clean [_]
  (b/delete {:path "target"}))

(defn ci "Run the CI pipeline of tests (and build the uberjar)." [_]
  (println "running")
  (clean nil)

  (b/process {:command-args ["npx" "shadow-cljs" "release" "main"]})
  
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})

  (b/compile-clj {:basis basis
                  :src-dirs ["src/main"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'ssorter.server-main}))
