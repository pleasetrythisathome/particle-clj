(ns particle-clj.system
  "Components and their dependency relationships"
  (:refer-clojure :exclude (read))
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [quil.core :as q]
   [particle-clj.quil :refer (new-quil-sketch)]))

(defn setup
  []
  {})

(defn draw
  [state]
  ;;(println state)
  (q/background 255)
  state)

(def start-sketch
  (partial new-quil-sketch
           :debug? true
           :display 1
           :size [800 600]
           :features [:keep-on-top]
           :setup setup
           :draw draw))
