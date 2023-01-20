(ns ssorter.server-components.middleware
  (:require
   [ssorter.server-components.config :refer [config]]
   [ssorter.server-components.phrag :refer [phrag]]
   [mount.core :refer [defstate]]
   [taoensso.timbre :as log]))

(defstate middleware
  :start (let [cfg (:middleware/TODO config)]
           (-> {}
               #_(add-ring-middleware))))
