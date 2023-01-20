(ns ssorter.server-components.middleware
  (:require
   [app.server-components.config :refer [config]]
   [app.server-components.phrag :refer [phrag]]
   [mount.core :refer [defstate]]
   [taoensso.timbre :as log]))

(defstate middleware
  :start (let [cfg (:middleware/TODO config)]
           (-> {}
               #_(add-ring-middleware))))
