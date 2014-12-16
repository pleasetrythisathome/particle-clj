(ns particle-clj.sketch
  (:require
   [plumbing.core :refer :all]
   [schema.core :as s]
   [quil.core :as q]
   [thi.ng.geom.core :as g]
   [thi.ng.geom.core.vector :as v]))

(defprotocol IRender
  (render [this]))

(defrecord Particle [pos]
  IRender
  (render [this]
    (apply q/point pos)))

(def new-particle-schema
  {:pos (s/pred v/vec2?)})

(defn new-particle [& {:as opts}]
  (->> opts
       (merge {:pos (v/vec2)})
       (s/validate new-particle-schema)
       (map->Particle)))

(defn random-particle [w h]
  (new-particle :pos (v/vec2 (rand-int w) (rand-int h))))

(defn setup
  []
  (let [num-particles 5000]
    (q/frame-rate 60)
    {:particles (for [_ (range num-particles)]
                  (random-particle (q/width) (q/height)))}))

(defn draw
  [state]
  ;;(println state)
  (q/background 255)

  (q/fill 0)
  (q/stroke-weight 2)
  (doseq [p (:particles state)]
    (render p))
  state)
