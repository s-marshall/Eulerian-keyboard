(defn draw-hexagon [translation]
  (q/with-translation [(:x translation) (:y translation)]
                        (hexagonal-key)))

(defn draw-square [translation offset]
  (let [center-position (* FULL-LENGTH (+ 0.5 (Math/cos THIRTY-DEG)))
        theta (* offset SIXTY-DEG)
        x (* center-position (Math/cos theta))
        y (* center-position (Math/sin theta))]
    (q/with-translation [(+ (:x translation) x) (+ (:y translation) y)]
                          (q/with-rotation [(* (- 6 offset) THIRTY-DEG)]

                                         (square-key)))))

(defn draw-triangle [translation offset]
  (let [theta (+ THIRTY-DEG (* offset SIXTY-DEG))
        x (* FULL-LENGTH (Math/cos theta))
        y (* FULL-LENGTH (Math/sin theta))]
    (q/with-translation [(+ (:x translation) x) (+ (:y translation) y)]
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
