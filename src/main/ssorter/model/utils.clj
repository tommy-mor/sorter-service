(ns ssorter.model.utils
  (:require [ssorter.server-components.db :refer [exec!]]
            [honey.sql.helpers :as h]))

(defn fill-out-id [table map]
  (def pk (keyword (name table) "id"))
  (def domain_pk (keyword (name table) "domain_pk"))
  (if (not (nil? (get map pk)))
    map
    (assoc map pk (-> (exec! (-> (h/select :id)
                                 (h/from table)
                                 (h/where [:=
                                           :domain_pk
                                           (get map domain_pk)])))
                      first :items/id))))

(comment
  (fill-out-id :items {:items/domain_pk "203fad0a-70d3-4548-89dd-170bc5713b8e"}))
