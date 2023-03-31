(ns ssorter.server-components.http-server
  (:require [ssorter.server-components.config :refer [config]]
            [ssorter.server-components.middleware :refer [middleware]]
            [mount.core :refer [defstate]]
            [taoensso.timbre :as log]
            [org.httpkit.server :as http]))

(defn app [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "hello HTTP!"})

(defstate http-server
  :start
  (let [cfg (:http-options config)]
    (log/info "starting http server with options" (pr-str cfg))
    (http/run-server middleware {:port (:port cfg)}))
  :stop (http-server))
