(ns ssorter.server-components.http-server
  (:require [ssorter.server-components.config :refer [config]]
            [ssorter.server-components.middleware :refer [middleware]]
            [ssorter.server-components.phrag :refer [phrag]]
            [mount.core :refer [defstate]]
            [taoensso.timbre :as log]
            [org.httpkit.server :as http]))


(defn app [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "hello HTTP!"})

(defstate http-server
  :start
  (let [cfg (:http-options/TODO config)]
    (log/info "starting http server with options" (pr-str cfg))
    (http/run-server app {:port 8080}))
  :stop (http-server))
