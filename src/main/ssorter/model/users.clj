(ns ssorter.model.users
  (:require
   [ssorter.server-components.db :refer [db]]
   [next.jdbc :as jdbc]
   [taoensso.timbre :as log]
   [honey.sql :as sql]
   [honey.sql.helpers :as h]
   [crypto.password.bcrypt :as password]
   [com.wsscode.pathom3.connect.operation :as pco]))


(pco/defresolver all-users [env input]
  {::pco/output [:all-users]} 
  {:all-users 3})

(def resolvers [all-users])


