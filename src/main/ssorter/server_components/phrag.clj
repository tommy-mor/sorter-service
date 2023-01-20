(ns ssorter.server-components.phrag
  (:require
   [app.server-components.config :refer [config]]
   [app.server-components.db :refer [db]]
   [mount.core :refer [defstate]]
   [taoensso.timbre :as log]))

(defstate phrag
  :start "starting phrag TODO")
