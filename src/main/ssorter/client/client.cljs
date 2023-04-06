(ns ssorter.client.client
  (:require
   [ssorter.client.ui :as ui]
   [ssorter.client.app :refer [app]]
   [ssorter.model.users :as users]
   
   [ssorter.model.integrations.linear :as linear]
   [ssorter.model.tags :as m.tags]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp]
   [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
   [com.fulcrologic.fulcro.ui-state-machines :as uism]

   
   [com.fulcrologic.fulcro.data-fetch :as df]
   
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]))

(defn ^:export init
  "Called by shadow-cljs upon initialization, see shadow-cljs.edn"
  []
  
  (app/set-root! app ui/Root {:initialize-state? true})
  (dr/initialize! app)
  (dr/change-route! app ["linear.issues"])
  (uism/begin! app users/session-machine ::users/session
               {:actor/login-form ui/Login
                :actor/current-session ui/Session})
  (app/mount! app
              (app/root-class app)
              "app"
              {:initialize-state? false}))

(defn ^:export refresh
  "Called by shadow-cljs upon initialization, see shadow-cljs.edn"
  []
  (println "refreshing app...")
  (comp/refresh-dynamic-queries! app)
  (app/mount! app (app/root-class app) "app"))
