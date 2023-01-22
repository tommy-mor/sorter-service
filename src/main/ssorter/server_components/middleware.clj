(ns ssorter.server-components.middleware
  (:require
   [ssorter.server-components.config :refer [config]]
   [mount.core :refer [defstate]]
   [taoensso.timbre :as log]))

(defstate middleware
  :start (let [cfg (:middleware/TODO config)]
           (-> {}
               #_(add-ring-middleware))))
