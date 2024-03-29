(ns script
  (:require [clojure.core.match :refer [match]]
            [org.httpkit.server :as server]
            [babashka.pods :as pods]
            [selmer.parser :as sel]))


(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")
(def fruits (clojure.edn/read-string (slurp "fruits.edn")))
(def id->fruit (into {} (map (juxt :id identity) fruits)))
(def fruit-votes (map (fn [v] (-> v
                                  (assoc :votes/left_item (id->fruit (:left_item_id v)))
                                  (assoc :votes/right_item (id->fruit (:right_item_id v)))))
                      (clojure.edn/read-string (slurp "fruit-votes.edn"))))

(sel/set-resource-path! ".")

(sel/cache-off!)

(defn render []
  (sel/render-file "tag-index.html" {:tag {:tags/title "epic tag!!"
                                          :tags/description "on second thought i am less epic than i thought, but still cool"}
                                    :items fruits
                                    :votes fruit-votes}))

(defn router [req]
  
  (let [paths (vec (rest (clojure.string/split (:uri req) #"/")))]
    (match [(:request-method req) paths]
           [:get []]
           {:body (render)}
           [:get ["output.css"]]
           {:body (slurp "./output.css")}
           
           :else {:body "404"})))

(def server (server/run-server router {:port 8080}))
@(promise)
