(ns particle-clj.sketch
  (:require
   [plumbing.core :refer :all]
   [schema.core :as s]
   [quil.core :as q]
   [thi.ng.geom.core :as g]
   [thi.ng.geom.core.vector :as v])
  (:import [processing.opengl PGraphicsOpenGL]))

(defprotocol IUpdate
  (-update [this]))

(defprotocol IRender
  (render [this]))

(defrecord Particle [pos vel]
  IUpdate
  (-update [this]
    (update this :pos g/+ vel))
  IRender
  (render [this]
    (apply q/point pos)))

(def new-particle-schema
  {:pos (s/pred v/vec2?)
   :vel (s/pred v/vec2?)})

(defn new-particle [& {:as opts}]
  (->> opts
       (merge {:pos (v/vec2)
               :vel (v/vec2)})
       (s/validate new-particle-schema)
       (map->Particle)))

(defn random-particle [w h]
  (new-particle :pos (v/vec2 (rand-int w) (rand-int h))
                :vel (v/randvec2)))

(defn setup
  []
  (let [num-particles 10000]
    (q/frame-rate 60)
    ;;(q/hint 'ENABLE_OPENGL_4X_SMOOTH)
    {:particles (for [_ (range num-particles)]
                  (random-particle (q/width) (q/height)))}))

(defn update-state
  [state]
  (-> state
      (update :particles (partial mapv -update))))


(defn draw
  [state]
  (q/background 255)

  (q/fill 0)
  (q/stroke-weight 2)
  (doseq [p (:particles state)]
    (render p))
  state)
