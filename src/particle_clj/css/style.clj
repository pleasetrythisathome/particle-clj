(ns particle-clj.css.style
  (:refer-clojure :exclude [+ - * /])
  (:require [garden.def :refer [defstylesheet defstyles]]
            [garden.units :refer [px]]
            [garden.core :refer [css]]
            [garden.color :refer [hsl rgb]]
            [garden.arithmetic :refer [+ - * /]]
            [garden.stylesheet :refer [at-media]]
            [clojure.string :as str]

            [particle-clj.css.utils :refer :all]
            [particle-clj.css.tools :refer :all]))

(defstyles style
  tools
  [:body
   {:margin "0px"}])
