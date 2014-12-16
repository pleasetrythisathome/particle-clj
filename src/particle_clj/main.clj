(ns particle-clj.main
  "Main entry point"
  (:require clojure.pprint)
  (:gen-class))

(defn -main [& args]
  ;; We eval so that we don't AOT anything beyond this class
  (eval '(do (require 'particle-clj.system)
             (require 'particle-clj.main)

             (println "Starting particle-clj")

             (let [system (particle-clj.system/start-sketch)]
               (println "System started")
               (println "Ready...")))))
