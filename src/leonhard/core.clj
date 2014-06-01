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
(load-file "src/leonhard/keyboard.clj")
(load-file "src/leonhard/sounds.clj")
(load-file "src/leonhard/processors.clj")
(load-file "src/leonhard/initialize-db.clj")

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
            component (last (flatten entity-component))
            component-color @(:color component)
            entity (first entity-component)

            screen-position (index->screen-position @(:xy-index component) edge-offset)
            component-name (:name component)]

        (q/fill (:R component-color) (:G component-color) (:B component-color))

        (condp = component-name
          :single-note (let [label @(:label component)
                             label-color (:label key-colors)]
                         (draw-hexagon screen-position)
                         (add-label label screen-position {:R label-color :G label-color :B label-color}))
          :dyad (let [category @(:category component)]
                  (draw-square screen-position category))
          :triad (let [category @(:category component)]
                  (draw-triangle screen-position category))))
      (recur (rest entities)))))

(defn play-key []
  (let [x (q/mouse-x)
        y (q/mouse-y)

        color (q/get-pixel x y)
        red-color (int (q/red color))

        key-pressed (cond
                     (= red-color (:label key-colors)) :hex
                     (= red-color (:hex key-colors)) :hex
                     (= red-color (:square key-colors)) :square
                     (= red-color (:up-tri key-colors)) :up-tri
                     (= red-color (:down-tri key-colors)) :down-tri
                     :else :ignore)]

    (when (not= :ignore key-pressed)
      (let [reference-key (closest-note {:x x :y y} key-indices 1000 {:x 0 :y 0})
            reference-position (index->screen-position reference-key edge-offset)
            category (chord-category key-pressed {:x x :y y} reference-position)

            key-type (key-pressed key-sounds)

            entities (entities-with-component key-type)
            local-keys (entities-satisfying reference-key :xy-index entities)

            entity-played (if (= key-type :single-note)
                            (first (first local-keys))
                            (first (first (entities-satisfying category :category local-keys))))

            possible-sounds (if (= key-type :single-note)
                              [@(:sound (last (first local-keys)))]
                              (into [] @(:sound (last (first (entities-satisfying category :category local-keys))))))
            p (println possible-sounds)]
        (play-notes possible-sounds)))))

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
