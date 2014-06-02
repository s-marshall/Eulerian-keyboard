(defn draw-hexagon [center-position color]
  (q/fill (:R color) (:G color) (:B color))
  (q/with-translation [(:x center-position) (:y center-position)]
                      (hexagonal-key)))

(defn draw-square [center-position offset color]
  (q/fill (:R color) (:G color) (:B color))
  (let [hyp (* FULL-LENGTH (+ 0.5 (Math/cos THIRTY-DEG)))
        theta (* offset SIXTY-DEG)
        x (* hyp (Math/cos theta))
        y (* hyp (Math/sin theta))]
    (q/with-translation [(+ (:x center-position) x) (+ (:y center-position) y)]
                          (q/with-rotation [(* (- offset 6) SIXTY-DEG)]
                                         (square-key)))))

(defn draw-triangle [center-position offset color]
  (q/fill (:R color) (:G color) (:B color))
  (let [hyp (+ FULL-LENGTH VERTEX-TO-CENTER)
        theta (+ THIRTY-DEG (* offset SIXTY-DEG))
        x (* hyp (Math/cos theta))
        y (* hyp (Math/sin theta))]
    (q/with-translation [(+ (:x center-position) x) (+ (:y center-position) y)]
                        (q/with-rotation [(* offset SIXTY-DEG)]
                                         (triangle-key)))))

(defn index->screen-position [xy-index edge-offset]
  (let [even-row-start (+ (* 1.5 FULL-LENGTH) (* 2 FULL-LENGTH (Math/cos THIRTY-DEG)))
        odd-row-start (+ FULL-LENGTH (* FULL-LENGTH (Math/cos THIRTY-DEG)))

        center-spacing (+ FULL-LENGTH (* 2 FULL-LENGTH (Math/cos THIRTY-DEG)))
        line-spacing  (+ FULL-LENGTH (* 1.5 FULL-LENGTH (Math/cos THIRTY-DEG)))

        offset (:x xy-index)

        x-position (if (even? (:y xy-index))
                     (+ (:x edge-offset) even-row-start (* offset center-spacing))
                     (+ (:x edge-offset) odd-row-start (* offset center-spacing)))

        y-position (+ (:y edge-offset) (* (:y xy-index) line-spacing))

        x (+ (:x edge-offset) x-position)
        y (+ (:y edge-offset) y-position)]
    {:x x-position :y y-position}))

(defn add-label [label position color]
  (q/fill (:R color) (:G color) (:B color))
  (q/text-size 100)
  (let [x-offset (if (= 1 (count label)) 40 68)
        y-offset 30]
    (q/text label (- (:x position) x-offset) (+ (:y position) y-offset))))

(defn absolute-value [x] (if (< x 0) (- 0 x) x))

(defn closest-note [mouse-position xy-indices min-distance best-index]
  (if (empty? xy-indices)
    best-index
    (let [x (:x mouse-position)
          y (:y mouse-position)
          current-index (first xy-indices)

          x-index (first current-index)
          y-index (last current-index)

          position (index->screen-position {:x x-index :y y-index} edge-offset)
          position {:x (* scaling-factor (:x position)) :y (* scaling-factor (:y position))}

          distance (+ (absolute-value (- x (:x position)))
                      (absolute-value (- y (:y position))))

          update? (if (< distance min-distance) true false)
          min-distance (if update? distance min-distance)
          best-index (if update? {:x x-index :y y-index} best-index)]
      (closest-note mouse-position (rest xy-indices) min-distance best-index))))

(defn find-range [x offset index]
  (let [x (mod x 360)
        lower-limit offset
        upper-limit (+ offset 60)]
  (if (and (< lower-limit x) (< x upper-limit))
    (mod index 6)
    (find-range x upper-limit (inc index)))))

(defn chord-category [key-type mouse-position reference-position]
  (let [delta-x (- (/ (:x mouse-position) scaling-factor) (:x reference-position))
        delta-y (- (/ (:y mouse-position) scaling-factor) (:y reference-position))
        quotient (/ delta-y delta-x)
        theta (/ (* (Math/atan quotient) 180) (Math/PI))
        theta (if (> delta-x 0) theta (+ theta 180))]
    (if (= key-type :square)
      (find-range theta -30 0)
      (find-range theta 0 0))))

(defn key-competitors [entity]
  (let [component (entity entity-component-db)
        competitors (if (= (:name component) :dyad)
                      (entity shared-dyads)
                      (entity shared-triads))]
    (conj competitors entity)))

(defn key-drawn? [key-sharing-entities]
  (let [drawn (filter #(true? @(:drawn (first (% entity-component-db)))) key-sharing-entities)]
    (if (empty? drawn) false true)))

(defn type-of-key [position]
  (let [x (:x position)
        y (:y position)

        color (q/get-pixel x y)
        red-color (int (q/red color))

        key-pressed (cond
                     (= red-color (:label key-colors)) :hex
                     (= red-color (:hex key-colors)) :hex
                     (= red-color (:square key-colors)) :square
                     (= red-color (:up-tri key-colors)) :up-tri
                     (= red-color (:down-tri key-colors)) :down-tri
                     :else :not-a-key)]
   key-pressed))

(defn play-key []
  (let [x (q/mouse-x)
        y (q/mouse-y)

        color (q/get-pixel x y)
        red-color (int (q/red color))

        key-pressed (type-of-key {:x x :y y})]

    (when (not= :not-a-key key-pressed)

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


