(ns ssorter.server-components.phrag
  (:require
   [ssorter.server-components.config :refer [config]]
   [ssorter.server-components.db :refer [db]]
   [mount.core :refer [defstate]]
   [taoensso.timbre :as log]
   [phrag.core :as phrag]
   [phrag.context]))

(def schema
  {:tables
   [{:name "users"}]})

(comment
  (def s (phrag.context/options->config {:db {:connection db}}))
  (phrag/schema s)
  (def x (-> (#'phrag/root-schema s)
             (#'phrag/update-relationships s)
             (#'phrag/update-views s)))

  schema)

(defn create-phrag []
  (let [config {:db {:connection db}}
        schema (phrag/schema config)]
    {:config config :schema schema}))

(defn handler [req]
  (let [{:keys [config schema]} (create-phrag)
        params  (:body-params req)
        query (:query params)
        vars (:variables params)]
    {:status 200
     :body (phrag/exec config schema query vars req)}))

(defstate phrag
  :start (do
           (log/info "starting phrag graphql interface")
           handler))
