(ns script
  (:require [clojure.core.match :as match]
            [org.httpkit.server :as server]
            [babashka.pods :as pods]))


(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")
(def fruits (clojure.edn/read-string (slurp "fruits.edn")))
(def fruit-votes (clojure.edn/read-string (slurp "fruit-votes.edn")))

(require '[pod.retrogradeorbit.bootleg.utils :refer [convert-to]]
         '[pod.retrogradeorbit.bootleg.enlive :refer [at content]]
         '[pod.retrogradeorbit.bootleg.mustache :refer [mustache]])

(defn load [fname]
  (convert-to (clojure.string/trim (slurp fname)) :hiccup))

(let [index-html (load "tag-page.html")
      item-html (load "item.html")
      vote-html (load "vote.html")
      items-panel-html (load "items-panel.html")
      tag-panel (load "tag-panel.html")
      votes-panel (load "votes-panel.html")]
  (spit "output.html"
        (convert-to
         (at index-html
             [:div#tag-panel] (content tag-panel)
             [:div#items-panel] (content items-panel-html)
             [:div#votes-panel] (content (at votes-panel
                                             [:div#list]
                                             (content (map
                                                       (fn [v] (mustache vote-html v :data :hiccup-seq))
                                                       fruit-votes)))))
         :html)))
