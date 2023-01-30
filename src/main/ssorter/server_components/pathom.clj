(ns ssorter.server-components.pathom 
  (:require [mount.core :refer [defstate]]
            [taoensso.timbre :as log]
            [com.wsscode.pathom3.connect.operation :as pco]
            [com.wsscode.pathom3.connect.runner :as pcr]
            [com.wsscode.pathom3.connect.indexes :as pci]
            [com.wsscode.pathom3.connect.built-in.plugins :as pbip]
            [com.wsscode.pathom3.interface.async.eql :as p.a.eql]
            [com.wsscode.pathom3.plugin :as p.plugin]
            
            [ssorter.model.users :as m.users]
            
            [ssorter.server-components.db :refer [db]]))

(def log-resolve-plugin
  {::p.plugin/id `log-resolve-plugin
   ::pcr/wrap-resolve
   (fn resolve-wrapper [resolve]
     (fn [env input]
       (log/info "pathom transaction" input)
       (resolve env input)))})

(def all-resolvers [m.users/resolvers])


(defn build-parser []
  (let [plugins [log-resolve-plugin
                 pbip/mutation-resolve-params]
        env (-> {::p.a.eql/paralell? true
                 :com.wsscode.pathom3.error/lenient-mode? true}
                (pci/register all-resolvers)
                (p.plugin/register plugins))]
    (log/info "building pathom3 parser")
    (fn parser [{:keys [ring/request] :as env'} tx]
      @(p.a.eql/process (merge env env') tx))))


(defstate parser
  :start (build-parser))



