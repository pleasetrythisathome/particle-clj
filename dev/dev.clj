(ns dev
  (:require
   [clojure.pprint :refer (pprint)]
   [clojure.reflect :refer (reflect)]
   [clojure.repl :refer (apropos dir doc find-doc pst source)]
   [clojure.tools.namespace.repl :refer (refresh refresh-all)]
   [clojure.core.async :refer [<!! timeout]]
   [particle-clj.quil :refer (wrap-draw)]
   [particle-clj.system :refer (start-sketch)]
   [quil.core :as q]))

(def stopped? (atom false))

(defn wrap-stopped [stopped-atom options]
  (wrap-draw (fn []
               (when @stopped-atom
                 (q/exit)))
             options))

(defn start
  "Starts the current development system."
  []
  (reset! stopped? false)
  (start-sketch :middleware [(partial wrap-stopped stopped?)]))

(defn stop
  "Shuts down and destroys the current development system."
  []
  (reset! stopped? true))

(defn go
  "Initializes the current development system and starts it stopped."
  []
  (start)
  :ok)

(defn reset []
  (stop)
  (<!! (timeout 500))
  (refresh :after 'dev/go))
