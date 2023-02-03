(ns ssorter.server-components.middleware
  (:require
   [ssorter.server-components.config :refer [config]]
   [ssorter.server-components.pathom :as pathom]
   [mount.core :refer [defstate]]
   [taoensso.timbre :as log]
   [ring.util.response :as util]
   [com.fulcrologic.fulcro.server.api-middleware :refer [handle-api-request]]
   #_[ssorter.server-components.phrag :refer [phrag]]
   [reitit.ring :as rring]
   [reitit.ring.middleware.exception :as exception]
   [muuntaja.core :as m]
   [muuntaja.middleware :as m.m]
   [reitit.ring.middleware.muuntaja :as muuntaja]))

(defn wrap-cors
  "add cors header to fix problem from running
  npx  diagnose-endpoint@1.1.0 --endpoint=http://localhost:8080"

  [handler]
  (fn [request]
    (-> (handler request)
        (util/header "access-control-allow-origin" "https://studio.apollographql.com")
        (util/header "access-control-allow-credentials" true)
        (util/header "access-control-allow-methods" "POST, GET, OPTIONS"))))


(defn wrap-exception
  "simple exception handling, could use something like reitit.ring.middleware.exception"
  [handler]
  (fn [request]
    (try (handler request)
         
         (catch Throwable e
           (log/error e)
           {:status 200
            :body "epic fail"}))))

(defn exception-handler [message exception request]
  {:status 500
   :body {:message message
          :exception (.getClass exception)
          :data (ex-data exception)
          :uri (:uri request)}})

(def exception-middleware
  (exception/create-exception-middleware
   (merge
    exception/default-handlers
    {java.lang.Throwable (partial exception-handler "throwable")
     clojure.lang.ExceptionInfo (partial exception-handler "exinfo")
     ::exception/default (partial exception-handler "default")
     ::exception/wrap (fn [handler e request]
                        (log/error (pr-str (:uri request)))
                        (handler e request))})))

(defn fulcro-handler [request]
  #_(log/info "fulcro request for" (:body-params request))
  (handle-api-request
   (:body-params request)
   (fn [tx] (pathom/parser {:ring/request request} tx))))

(def router
  (rring/router [["/" {:get {:handler (constantly {:status 200 :body ":3"})}}]
                 ["/api/v1" {:post {:handler fulcro-handler}
                             :get {:handler (constantly {:status 200 :body "apiiii"})}}]
                 #_["/graphql" {:get {:handler (constantly
                                                {:status 200 :body
                                                 (slurp (io/resource "templates/gql.html"))})}
                                :post {:handler phrag}}]]
                {:data {:muuntaja m/instance
                        :middleware [m.m/wrap-exception ;; REEEEEEEEEEEEEEEEEEEEEEEEEEEEE pls help me.....
                                     muuntaja/format-negotiate-middleware
                                     
                                     muuntaja/format-response-middleware
                                     exception-middleware
                                     muuntaja/format-request-middleware]}}))

(defstate middleware
  :start (let [cfg (:middleware/TODO config)]
           (rring/ring-handler router (rring/create-default-handler))))

(comment (middleware {:request-method :get
                      :uri "/api/math"})
         (middleware {:request-method :get
                      :uri "/epic"}))
