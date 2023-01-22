(ns ssorter.server-components.middleware
  (:require
   [ssorter.server-components.config :refer [config]]
   [mount.core :refer [defstate]]
   [taoensso.timbre :as log]
   [ring.util.response :as util]
   [ssorter.server-components.phrag :refer [phrag]]
   [muuntaja.middleware :as mtja]))

(defn wrap-cors
  "add cors header to fix problem from running
  npx  diagnose-endpoint@1.1.0 --endpoint=http://localhost:8080"

  [handler]
  (fn [request]
    (-> (handler request)
        (util/header "access-control-allow-origin" "https://studio.apollographql.com")
        (util/header "access-control-allow-credentials" true))))


(def ^:private not-found-handler
  (fn [req]
    {:status  404
     :headers {"Content-Type" "text/plain"}
     :body    "NOPE"}))

(defn wrap-exception
  "simple exception handling, could use something like reitit.ring.middleware.exception"
  [handler]
  (fn [request]
    (try (handler request)
         (catch Throwable e
           (log/error e)
           {:status 500
            :body (pr-str e)}))))




(defstate middleware
  :start (let [cfg (:middleware/TODO config)]
           (-> phrag
               wrap-cors
               mtja/wrap-format
               wrap-exception)))
