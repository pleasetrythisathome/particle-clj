(ns particle-clj.quil
  (:require
   [clojure.tools.logging :as log]
   [com.stuartsierra.component :as component]
   [plumbing.core :refer :all]
   [schema.core :as s]
   [clojure.string :refer [join]]
   [clojure.stacktrace :refer [print-cause-trace]]
   [quil.core :as q]
   [quil.util :as u]
   [quil.middleware :as m]))

;; ========== Middleware ==========

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

(defn wrap-stopped [stopped-atom options]
  (wrap-draw (fn []
               (when @stopped-atom
                 (q/exit)))
             options))

;; (defn- wrap-fn [pause name function do-on-pause]
;;   (fn [& args]
;;     (if @pause
;;       (do-on-pause)
;;       (try
;;         (apply function args)
;;         (catch Exception e
;;           (println "Exception in " name " function: " e "\nstacktrace: " (with-out-str (print-cause-trace e)))
;;           (reset! pause {:name name
;;                          :exception e
;;                          :time (java.util.Date.)}))))))

;; (defn- draw-error-message [pause]
;;   (let [error (q/create-graphics (q/width) (q/height))]
;;     (q/with-graphics error
;;       (q/push-style)
;;       (q/background 255)
;;       (q/fill 0)
;;       (q/text-size 15)
;;       (let [{:keys [name exception time]} @pause
;;             str [(format "Sketch was paused due to an exception thrown in %s" name)
;;                  (str "Check REPL or console for stacktrace")
;;                  (str "Time: " time)
;;                  (str "Fix the error and then press any key to unpause sketch.")]]
;;         (q/text (join \newline str) 10 20))
;;       (q/pop-style))
;;     (q/set-image 0 0 error)))

;; (defn- wrap-draw [options pause]
;;   (let [draw (:draw options identity)]
;;     (assoc options
;;       :draw (wrap-fn pause :draw draw #(draw-error-message pause)))))

;; (defn- unpause [pause]
;;   (reset! pause nil))

;; (defn- wrap-key-pressed [options pause]
;;   (let [key-pressed (:key-pressed options (fn [s e] s))]
;;     (assoc options
;;       :key-pressed (wrap-fn pause :key-pressed key-pressed #(unpause pause)))))

;; (defn pause-on-error
;;   "Pauses sketch if any of user-provided handlers throws error."
;;   [options]
;;   (let [pause (atom nil)]
;;     (-> (into {}
;;               (for [[name value] options]
;;                 [name (if (and (u/callable? value)
;;                                (not= name :draw)
;;                                (not= name :key-pressed))
;;                         (wrap-fn pause name value (fn []))
;;                         value)]))
;;         (wrap-draw pause)
;;         (wrap-key-pressed pause))))

;; ========== Schema ==========

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

;; ========== Component ==========

(defrecord QuilSketch [stopped? options]
  component/Lifecycle
  (start [this]
    (let [stopped? (atom false)]
      (->> options
           (<- (update :middleware conj (partial wrap-stopped stopped?)))
           (mapcat identity)
           (apply q/sketch))
      (assoc this
        :stopped? stopped?)))
  (stop [this]
    (when-let [stopped? (:stopped? this)]
      (reset! stopped? true))))

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
       (hash-map :options)
       (map->QuilSketch)))
