(defproject particle-clj "0.1.0-SNAPSHOT"
  :description "A modular project created with lein new modular hello-world-web"
  :url "http://github.com/pleasetrythisathome/particle-clj"

  :exclusions [com.stuartsierra/component]

  :dependencies
  [
   [com.stuartsierra/component "0.2.2"]
   [juxt.modular/maker "0.5.0"]
   [juxt.modular/wire-up "0.5.0"]
   [org.clojure/clojure "1.7.0-alpha4"]
   [org.clojure/tools.reader "0.8.9"]
   [prismatic/plumbing "0.2.2"]
   [prismatic/schema "0.2.1"]
   ]

  :main particle-clj.main

  :repl-options {:init-ns user
                 :welcome (println "Type (dev) to start")}

  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.5"]]
                   :source-paths ["dev"
                                  ]}})
