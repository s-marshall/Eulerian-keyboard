(definst triangle-wave [freq 440 attack 0.01 sustain 0.1 release 0.4 vol 0.4]
  (* (env-gen (lin-env attack sustain release) 1 1 0 1 FREE)
     (lf-tri freq)
     vol))

(defn triangle2 [music-note]
  (triangle-wave (midi->hz (note music-note))))

(def midi-info (connected-midi-devices))

(defn play-note [n]
  (if (empty? midi-info)
    ;(piano (note n)) ; crappy sounding
    (triangle2 n)
    (let [synth (midi-out (:name (first midi-info)))]
      (midi-note-on synth (note n) 80))))

(defn play-notes [sounds-to-play]
  (loop [sounds sounds-to-play]
    (when ((complement empty?) sounds)
      (play-note (first sounds))
      (recur (rest sounds)))))

