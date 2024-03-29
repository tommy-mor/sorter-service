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
            [ssorter.model.votes :as m.votes]
            [ssorter.model.items :as m.items]
            [ssorter.model.tags :as m.tags]
            [ssorter.model.membership :as m.membership]
            [ssorter.model.sorted :as m.sorted]
            [ssorter.model.integrations.linear :as m.i.linear]
            [ssorter.model.pairs :as m.pairs]
            
            [clojure.walk]))

(def log-resolve-plugin
  {::p.plugin/id `log-resolve-plugin
   ::pcr/wrap-resolve
   (fn resolve-wrapper [resolve]
     (fn [env input]
       #_(log/debug "pathom transaction" input)
       (resolve env input)))})

(def all-resolvers [m.users/resolvers
                    m.votes/resolvers
                    m.items/resolvers
                    m.tags/resolvers
                    m.sorted/resolvers
                    m.pairs/resolvers
                    m.i.linear/resolvers
                    m.membership/resolvers])


(defn clean-exceptions "takes a pathom3 response, and looks for throwables. converts them to maps"
  [resp]
  (clojure.walk/postwalk #(if (instance? java.lang.Throwable %)
                            (do
                              (log/error %)
                              (select-keys (Throwable->map %) [:cause :data]))
                            %)
                         resp))

(defn build-parser []
  (let [plugins [log-resolve-plugin
                 pbip/mutation-resolve-params]
        env (-> {::p.a.eql/paralell? true
                 :com.wsscode.pathom3.error/lenient-mode? true}
                (pci/register all-resolvers)
                (p.plugin/register plugins))]
    (log/info "building pathom3 parser")
    (fn parser [{:keys [ring/request] :as env'} starting-data tx]
      (clean-exceptions @(p.a.eql/process (merge env env') starting-data tx)))))


(defstate parser
  :start (build-parser))



