(ns ssorter.model.users
  (:require
   [ssorter.server-components.db :refer [db exec!]]
   [next.jdbc :as jdbc]
   [taoensso.timbre :as log]
   [honey.sql :as sql]
   [honey.sql.helpers :as h]
   [crypto.password.bcrypt :as password]
   [com.wsscode.pathom3.connect.operation :as pco]
   [com.fulcrologic.fulcro.server.api-middleware :as fmw]))

(comment (->> (exec! (-> (h/select :*)
                         (h/from :users)
                         (h/where true)))
              first
              :users/password_hash
              (password/check "epicwin" ))
         
         (exec! (-> (h/update :users)
                    (h/where [:= :user_name "tommy" ])
                    (h/set {:password_hash (password/encrypt "epicwin")} ))))


(pco/defresolver current-session-resolver [env input]
  {::pco/output [{::current-session [:session/valid? :users/user_name]}]}
  (let [{:keys [account/name session/valid?]} (get-in env [:ring/request :session])]
    (if valid?
      (do
        (log/info name "already logged in!")
        {::current-session {:session/valid? true :users/user_name name}})
      {::current-session {:session/valid? false}})))


(defn response-updating-session
  "Uses `mutation-response` as the actual return value for a mutation, but also stores the data into the (cookie-based) session."
  [mutation-env mutation-response]
  (let [existing-session (some-> mutation-env :ring/request :session)]
    (fmw/augment-response
     mutation-response
     (fn [resp]
       (let [new-session (merge existing-session mutation-response)]
         (assoc resp :session new-session))))))


(pco/defmutation login [env {:keys [username password]}]
  {::pco/output [:session/valid? :users/user_name]}
  (log/info "Authenticating" username)
  
  (let [hash (-> (h/select :password_hash)
                 (h/from :users)
                 (h/where [:= :user_name username])
                 exec! first :users/password_hash)]
    (if (password/check password hash)
      (response-updating-session env
                                 {:session/valid? true
                                  :account/name   username})
      (do
        (log/error "Invalid credentials supplied for" username)
        (throw (ex-info "Invalid credentials" {:username username}))))))


(pco/defmutation logout [env params]
  {::pco/output [:session/valid?]}
  (response-updating-session env {:session/valid? false :account/name ""}))


(def resolvers [current-session-resolver login logout])


