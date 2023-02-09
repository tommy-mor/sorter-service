(ns ssorter.model.integrations.linear
  (:require [org.httpkit.client :as http]
            [org.httpkit.sni-client :as sni-client]
            
            [ssorter.server-components.db :refer [exec!]]
            [ssorter.server-components.config :refer [config]]
            [taoensso.timbre :as log]
            [honey.sql.helpers :as h]
            [com.wsscode.pathom3.connect.built-in.resolvers :as pbir]
            [com.wsscode.pathom3.connect.operation :as pco]

            [com.wsscode.pathom3.connect.indexes :as pci]
            [com.wsscode.pathom3.interface.eql :as p.eql]
            
            [graphql-query.core :refer [graphql-query]]

            [jsonista.core :as j]
            [clojure.walk :refer [prewalk]]))

(def mapper
  (j/object-mapper
   {:encode-key-fn name
    :decode-key-fn keyword}))

(def linear-api "https://api.linear.app/graphql")
(def linear-key (:linear/api (clojure.edn/read-string (slurp "secrets.edn"))))
(alter-var-root #'org.httpkit.client/*default-client* (fn [_] sni-client/default-client))

(defn linear-req [m & [arg]]
  (try (let [req @(http/request {:method :post
                                 :url linear-api
                                 :headers {"Content-Type" "application/json"
                                           "Authorization" linear-key}
                                 :body (j/write-value-as-string (merge {:query
                                                                        (if (string? m)
                                                                          m
                                                                          (graphql-query
                                                                           m))}
                                                                       arg))})
             body (:body req)]
         (j/read-value body mapper))
       (catch Exception e
         e)))


(defn wrap-keywords [coll]
  (prewalk (fn [x] (if (keyword? x)
                     (keyword "ssorter.model.integrations.linear" (name x ))
                     x))
           coll))

(comment (wrap-keywords {:x/x 4}))

(pco/defresolver issues [env _]
  {::pco/output [{::issues [::id
                            ::title
                            ::description
                            ::createdAt
                            ::estimate
                            ::priorityLabel
                            ::children
                            ::identifier]}]}
  (let [{:keys [before after onlyParents?] :as param} (pco/params env)
        params (cond before {:before before :last 10}
                   after {:after after :first 10}
                   :else {:first 10})

        params (merge params
                      (if onlyParents?
                        {:filter {:children {:length {:gt 0.0}}}}
                        {}))
        ]
    (def page page)
    
    (let [req (-> (linear-req {:queries
                               [[:issues params
                                 [[:nodes [:id
                                           :title
                                           :description
                                           :createdAt
                                           :priorityLabel
                                           :estimate
                                           :identifier
                                           [:children [[:nodes [:id]]]]]]]]]})
                  :data :issues :nodes wrap-keywords)]
      (def x req)
      {::issues (if before (reverse req) req)})))


(comment (-> (issues) ::issues first ::id))

(def resolvers [issues])



