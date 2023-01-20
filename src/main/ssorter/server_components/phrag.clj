(ns ssorter.server-components.phrag
  (:require
   [ssorter.server-components.config :refer [config]]
   [ssorter.server-components.db :refer [db]]
   [mount.core :refer [defstate]]
   [taoensso.timbre :as log]))

(defstate phrag
  :start "starting phrag TODO")
