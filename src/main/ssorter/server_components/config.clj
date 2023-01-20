(ns ssorter.server-components.config
  (:require
    [mount.core :refer [defstate args]]
    [taoensso.timbre :as log]))


(defn configure-logging! [config]
  (let [{:keys [taoensso.timbre/logging-config]} config]
    (log/info "Configuring Timbre with " logging-config)
    (log/merge-config! logging-config)))


(defstate config
  :start (let [{:keys [config] :or {config "config/dev.edn"}} (args)
               configuration (slurp config)]
           (log/info "Loaded config" configuration)
           configuration))
