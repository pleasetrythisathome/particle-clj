(ns particle-clj.quil
  (:require
   [clojure.tools.logging :as log]
   [plumbing.core :refer :all]
   [schema.core :as s]
   [quil.core :as q]
   [quil.middleware :as m]))

(defn wrap-draw [f {:keys [draw]
                    :or {draw (fn [])}
                    :as options}]
  (let [updated-draw (fn [s]
                       (draw s)
                       (f))]
    (assoc options :draw updated-draw)))

(defn show-frame-rate [options]
  (wrap-draw (fn []
               (q/fill 0)
               (q/text-num (q/current-frame-rate) 10 10))
             options))

(def sketch-fn-schema (s/make-fn-schema {} [[{}]]))
(def event-fn-schema (s/make-fn-schema {} [[{} s/Any]]))

(def options
  {:title s/Str
   :size (s/both [s/Int] (s/pred #(= 2 (count %))))
   :renderer (s/enum :p2d :p3d :pdf :opengl)
   :output-file s/Str
   :display s/Int
   :features [(s/enum :keep-on-top
                      :exit-on-close
                      :resizable
                      :no-safe-fns
                      :present
                      :no-start)]
   :bgcolor s/Str
   :middleware [(s/make-fn-schema s/Any [[]])]})

(def sketch-fns
  [:setup
   :draw
   :focus-gained
   :focus-lost
   :on-close])

(def mouse-events
  [:mouse-entered
   :mouse-exited
   :mouse-pressed
   :mouse-released
   :mouse-clicked
   :mouse-moved
   :mouse-dragged
   :mouse-wheel])

(def key-events
  [:key-pressed
   :key-released
   :key-typed])

(def new-quil-sketch-schema
  (->> (merge options
              (zipmap sketch-fns (repeat sketch-fn-schema))
              (zipmap (concat mouse-events key-events) (repeat event-fn-schema)))
       (map-keys s/optional-key)))

(defn new-quil-sketch [& {:keys [fun-mode? debug?] :as opts}]
  (->> (dissoc opts :fun-mode? :debug?)
       (merge {:setup (constantly {})
               :draw identity})
       (<- (update :middleware (fn [middleware]
                                 (->> (or middleware [])
                                      (cons m/fun-mode)
                                      (into [])
                                      (<- (?> debug? (conj ;;m/pause-on-error
                                                           show-frame-rate)))))))
       (s/validate new-quil-sketch-schema)
       (mapcat identity)
       (apply q/sketch)))
