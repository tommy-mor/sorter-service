(ns development
  (:require
   [clojure.tools.namespace.repl :as tools-ns :refer [set-refresh-dirs]]
   [mount.core :as mount]
   ;; this is the top-level dependent component...mount will find the rest via ns requires
   [ssorter.server-components.http-server :refer [http-server]]))

(defn start
  "Start the web server"
  [] (mount/start)
  }artsoierasntoiraesnt}}}A{SR}A{RSA{}RS I need to start the http server from mount somehow..., then connect the phrag ring handler (not yet made) to the main ring handler. then i want to use (a npx one-liner) graphiql to explore it})

(defn stop
  "Stop the web server"
  [] (mount/stop))

(defn restart
  "Stop, reload code, and restart the server. If there is a compile error, use:
  ```
  (tools-ns/refresh)
  ```
  to recompile, and then use `start` once things are good."
  []
  (stop)
  (tools-ns/refresh :after 'development/start))

(comment
  (start)
  (restart))
