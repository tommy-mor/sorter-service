(ns script
  (:require [clojure.string :as str]
            [org.httpkit.client :as http]
            [org.httpkit.sni-client :as sni-client]
            [cheshire.core :as json]
            [babashka.cli :as cli]
            [clojure.java.io]
            [clojure.edn]
            [cognitect.transit :as transit])
  (:import
   [java.io ByteArrayInputStream ByteArrayOutputStream]))

(defn transit-str [inp]
  (let [out (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json)]
    
    (transit/write writer inp)
    (.toString out)))

(defn transit-read [str]
  (let [in (ByteArrayInputStream. (.getBytes str))
        reader (transit/reader in :json)]
    
    (transit/read reader)))

(defn req [inp]
  (println (transit-str inp))
  (let [req @(http/request {:method :post
                            :url "http://localhost:8080/api/v1"
                            :headers {"Content-Type" "application/transit+json"
                                      "accept" "application/transit+json"}
                            :body (transit-str inp)})]
    (println req)
    (println (:body req))
    (transit-read (:body req))))

(req [:all-users])
