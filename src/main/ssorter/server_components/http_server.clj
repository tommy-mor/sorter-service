(ns ssorter.server-components.http-server
  (:require [ssorter.server-components.config :refer [config]]
            [ssorter.server-components.middleware :refer [middleware]]
            [mount.core :refer [defstate]]
            [taoensso.timbre :as log]))

(defstate http-server
  :start
  (let [cfg (:http-options/TODO config)]
    (log/info "starting http server with options" (pr-str cfg))
    4)
  :stop (+ http-server 8))
