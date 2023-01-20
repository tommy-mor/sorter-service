(ns user
  (:require
   [clojure.tools.namespace.repl :as tools-ns :refer [set-refresh-dirs]]))

(set-refresh-dirs "src/main" "src/dev" "src/test")

(comment
  (require 'development)
  (in-ns 'development)
  (restart))

