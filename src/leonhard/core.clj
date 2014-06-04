(ns leonhard.core
  (:use overtone.live [overtone.inst.piano])
  (:require [quil.core :as q]
            [clojure.math.combinatorics :as combo]
            [clojure.tools.trace :as tool]))

(def input (atom :drag))
(def key-colors {:hex 0 :square 204 :up-tri 150 :down-tri 82 :label 255})
(def key-sounds {:hex :single-note :square :dyad :up-tri :triad :down-tri :triad})

(def ^:const scaling-factor 0.35)
(def ^:const background-color 40)
(def edge-offset {:x 150 :y 250})
(def key-indices (for [x (range 12) y (range 9)] [x y]))

(load-file "src/leonhard/entity-component.clj")
(load-file "src/leonhard/initialize-db.clj")
(load-file "src/leonhard/keyboard.clj")
(load-file "src/leonhard/sounds.clj")
(load-file "src/leonhard/processors.clj")

(defn setup []
  (q/smooth)
  (q/frame-rate 60)
  (q/background background-color)
  (q/stroke background-color))

(defn draw []
  (q/scale scaling-factor)

  (loop [entities entity-component-db]

    (when ((complement empty?) entities)
      (let [entity-component (first entities)
            entity (first entity-component)
            component (first (entity entity-component-db))
            component-color @(:color component)

            screen-position (index->screen-position @(:xy-index component) edge-offset)]

        (condp = (:name component)
          :single-note (let [label @(:label component)
                             label-color (:label key-colors)]
                         (draw-hexagon screen-position component-color)
                         (add-label label screen-position {:R label-color :G label-color :B label-color}))
          :dyad (let [category @(:category component)]
                  (draw-square screen-position category component-color))
          :triad (let [category @(:category component)]
                   (draw-triangle screen-position category component-color)))
      (recur (rest entities))))))

(defn mouse-clicked [] ; Click to play note or chords
  (when (= @input :click)
    (play-key)))

(defn mouse-moved [] ; Drag mouse to play
  (when (= @input :drag)
    (play-key)))

(defn key-pressed [] ; Press any key to switch between click and drag
  (if (= @input :click)
    (swap! input (fn [x] :drag))
    (swap! input (fn [x] :click))))

(q/defsketch leonhard
             :title "Eulerian Keyboard"
             :setup setup
             :draw draw
             :size [1370 820]
             :mouse-moved mouse-moved
             :mouse-clicked mouse-clicked
             :key-pressed key-pressed)
