(ns ssorter.server-components.http-server
  (:require [app.server-components.config :refer config]
            [app.server-components.middleware :refer middleware]
            [mount.core :refer [defstate]]
            [taoensso.timbre :as log]))

(defstate http-server
  :start
  (let [cfg (:http-options/TODO config)]
    (log/info "starting http server with options" (pr-str cfp))
    4)
  :stop (+ http-server 8))
