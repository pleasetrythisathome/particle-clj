(load-file "./build.util.clj")
(require '[build.util :as build])

(set-env!
 :name 'particle-clj
 :version "0.1.0-SNAPSHOT"
 :description "particle system performance tests"
 :url "http://github.com/pleasetrythisathome/particle-clj"
 :dependencies (build/deps)
 :src-paths    #{"src"}
 :rsc-paths    #{"resources"})

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[boot-garden.core      :refer [garden]]
 '[deraen.boot-cljx      :refer [cljx]]
 '[pandeiro.http         :refer [serve]]
 '[clojure.tools.namespace.repl :refer [set-refresh-dirs]])

(deftask development
  "watch and compile cljx, css, cljs, init cljs-repl and push changes to browser"
  []
  (let [src (:src-paths (get-env))]
    (set-env! :src-paths (conj src "dev")))
  (apply set-refresh-dirs (get-env :src-paths))
  (comp (serve :dir "target")
        (watch)
        ;; (speak)
        (cljx)
        (garden :styles-var 'particle-clj.css.style/style
                :output-to "public/css/style.css"
                :pretty-print true)
        (cljs-repl)
        (cljs :output-to "public/js/main.js"
              :optimizations :none
              :unified true
              :source-map true
              :pretty-print true)
        (reload)))

(defn dev
  []
  (require 'dev)
  (in-ns 'dev))
