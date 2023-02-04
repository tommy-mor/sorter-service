(ns ssorter.client.client
  (:require
   [ssorter.client.ui :as ui]
   [ssorter.client.app :refer [app]]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp]
   [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]))

(defn ^:export init
  "Called by shadow-cljs upon initialization, see shadow-cljs.edn"
  []
  
  (app/set-root! app ui/Root {:initialize-state? true})
  (dr/initialize! app)
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




