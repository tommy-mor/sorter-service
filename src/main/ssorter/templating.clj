(ns ssorter.templating
  (:require [ssorter.server-components.db :refer [db exec!]]
            [honey.sql.helpers :as h]))

(defn make-example-files! []
  (spit "fruits.edn" (with-out-str (clojure.pprint/pprint (exec! (-> (h/select :*)
                                                                     (h/from :items))))))
  (spit "fruit-votes.edn" (with-out-str (clojure.pprint/pprint (exec! (-> (h/select :*)
                                                                          (h/from :votes)))))))

(comment
  (make-example-files!))



