(ns particle-clj.system
  "Components and their dependency relationships"
  (:refer-clojure :exclude (read))
  (:require
   [clojure.java.io :as io]
   [clojure.tools.reader :refer (read)]
   [clojure.string :as str]
   [clojure.tools.reader.reader-types :refer (indexing-push-back-reader)]
   [com.stuartsierra.component :refer (system-map system-using using)]
   [modular.maker :refer (make)]))

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
  (config-from (io/file (System/getProperty "user.home") ".particle-clj.edn")))

(defn ^:private config-from-classpath
  []
  (if-let [res (io/resource "particle-clj.edn")]
    (config-from (io/file res))
    {}))

(defn config
  "Return a map of the static configuration used in the component
  constructors."
  []
  (merge (config-from-classpath)
         (user-config)))


(defn new-system-map
  [config]
  (apply system-map
    (apply concat
      (-> {}))))

(defn new-dependency-map
  []
  {})

(defn new-co-dependency-map
  []
  {})

(defn new-production-system
  "Create the production system"
  []
  (-> (new-system-map (config))
      (system-using (new-dependency-map))))
