(ns ssorter.server-components.config
  (:require
    [mount.core :refer [defstate args]]
    [taoensso.timbre :as log]
    [clojure.edn]))


(defn configure-logging! [config]
  (let [{:keys [taoensso.timbre/logging-config]} config]
    (log/info "Configuring Timbre with " logging-config)
    (log/merge-config! logging-config)))


(defstate config
  :start (let [{:keys [config] :or {config "src/main/config/dev.edn"}} (args)
               configuration (slurp config)
               config (merge
                       (clojure.edn/read-string configuration)
                       (clojure.edn/read-string (slurp "secrets.edn")))]
           (log/info "Loaded config" config)
           config))
