(ns ssorter.server-components.phrag
  (:require
   [ssorter.server-components.config :refer [config]]
   [ssorter.server-components.db :refer [db]]
   [mount.core :refer [defstate]]
   [taoensso.timbre :as log]
   [phrag.core :as phrag]))

(defstate phrag
  :start "starting phrag TODO")
