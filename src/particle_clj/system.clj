(ns particle-clj.system
  "Components and their dependency relationships"
  (:refer-clojure :exclude (read))
  (:require
   [clojure.java.io :as io]
   [clojure.tools.reader :refer (read)]
   [clojure.tools.reader.reader-types :refer (indexing-push-back-reader)]
   [clojure.string :as str]
   [com.stuartsierra.component :refer (system-map system-using using)]
   [modular.maker :refer (make)]
   [particle-clj.quil :refer (new-quil-sketch)]
   [particle-clj.sketch :as sketch]))


;; ========== Config ==========

(defn ^:private read-file
  [f]
  (read
   ;; This indexing-push-back-reader gives better information if the
   ;; file is misconfigured.
   (indexing-push-back-reader
    (java.io.PushbackReader. (io/reader f)))))

(defn ^:private config-from
  [f]
  (if (.exists f)
    (read-file f)
    {}))

(defn ^:private user-config
  []
  (config-from (io/file (System/getProperty "user.home") ".populace.edn")))

(defn ^:private config-from-classpath
  []
  (if-let [res (io/resource "populace.edn")]
    (config-from (io/file res))
    {}))

(defn config
  "Return a map of the static configuration used in the component
  constructors."
  []
  (merge (config-from-classpath)
         (user-config)))

;; ========= System =========

(defn new-system-map
  [config]
  (system-map
   :quil (make new-quil-sketch config
               :debug? true
               :display 0
               :size [800 600]
               :features [:keep-on-top]
               :setup #'sketch/setup
               :draw #'sketch/draw)))

(defn new-dependency-map
  []
  {})

(defn new-production-system
  "Create the production system"
  []
  (-> (new-system-map (config))
      (system-using (new-dependency-map))))
