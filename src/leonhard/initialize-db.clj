(def circle-of-fifths ["Eb" "Bb" "F" "C" "G" "D" "A" "E" "B" "F#" "Db" "Ab"])
(def first-element-in-row ["Eb" "B" "D" "Bb" "Db" "A" "C" "Ab" "B"])

(defn generate-note-names [names first-element-in-row circle-of-fifths]
  (if (empty? first-element-in-row)
    names
    (let [start-position (.indexOf circle-of-fifths (first first-element-in-row))
          names (into names [(map #(nth circle-of-fifths (mod % 12)) (range start-position (+ 12 start-position)))])]
      (generate-note-names names (rest first-element-in-row) circle-of-fifths))))

(def note-labels (generate-note-names [] first-element-in-row circle-of-fifths))

(defn single-note-sound [xy-index]
  (let [x (:x xy-index)
        y (:y xy-index)
        octave (- 8 y)
        note-name (nth (nth note-labels y) x)]
    (keyword (str note-name octave))))

(defn get-octave [number]
  (- 8 (max (min number 8) 0)))

(defn get-note-name [note-symbol]
  (let [note-string (name note-symbol)
        octave (re-find #"\d" note-string)
        end (.indexOf note-string octave)]
    (subs note-string 0 end)))

(defn dyad-sound [reference-note category]
  (let [x (:x reference-note)
        y (:y reference-note)
        first-sound (single-note-sound reference-note)
        note (get-note-name first-sound)
        index (.indexOf circle-of-fifths note)

        note-2 (condp = category
                 0 {:note (inc index) :octave y}
                 1 {:note (+ index 9) :octave (inc y)}
                 2 {:note (+ index 8) :octave (inc y)}
                 3 {:note (+ index 11) :octave y}
                 4 {:note (+ index 3) :octave (dec y)}
                   {:note (+ index 4) :octave (dec y)})
        second-index (mod (:note note-2) 12)
        second-note (nth circle-of-fifths second-index)
        octave (get-octave (:octave note-2))

        second-sound (keyword (str second-note octave))]
    [first-sound second-sound]))

(defn triad-sound [reference-note category]
  (distinct (flatten [(dyad-sound reference-note category)
                      (dyad-sound reference-note (mod (inc category) 6))])))

(component single-note
           [xy-index color label]
           {:xy-index (atom xy-index)
            :sound (atom (single-note-sound xy-index))
            :color (atom color)
            :label (atom label)})

(component dyad
           [xy-index category]
           {:xy-index (atom xy-index)
            :category (atom category)
            :color (atom (let [color (:square key-colors)]
                           {:R color :G color :B color}))
            :sound (atom (into () (dyad-sound xy-index category)))})

(component triad
           [xy-index category]
           {:xy-index (atom xy-index)
            :category (atom category)
            :color (atom (let [color (if (odd? category)
                                 (:up-tri key-colors)
                                 (:down-tri key-colors))]
                     {:R color :G color :B color}))
            :sound (atom (triad-sound xy-index category))})

(defn update-db [db components]
  (if (empty? components)
    db
    (let [component (first components)]
      (update-db (add-component-to-entity db component (entity)) (rest components)))))

(defn create-ads [func-ad indices ads]
  (if (empty? indices)
    ads
    (let [i (first indices)
          ad (func-ad {:x (:x i) :y (:y i)} (:index i))
          notes (:sound ad)]
      (create-ads func-ad (rest indices) (conj ads ad)))))

(defn contains-note [note ad]
  (condp = ad
    :single-note (filter #(= note (:sound (last %))) (entities-with-component ad))
    (filter #(some #{note} (:sound (last %))) (entities-with-component ad))))

(def indices
  (for [x (range 12) y (range 9) index (range 6)]
    {:x x :y y :index index}))

(def single-notes (for [x (range 12) y (range 9)
                        :let [key-color (if (= "C" (nth (nth note-labels y) x))
                                          {:R 0 :G 0 :B 255}
                                          {:R (:hex key-colors) :G (:hex key-colors) :B (:hex key-colors)})]]
                    (single-note {:x x :y y} key-color (nth (nth note-labels y) x))))
(def dyads (create-ads dyad indices []))
(def triads (create-ads triad indices []))

(def entity-component-db (hash-map))
(def entity-component-db
  (update-db entity-component-db (into single-notes (into dyads triads))))

(def dyad-entities (entities-with-component :dyad))
(def triad-entities (entities-with-component :triad))

(defn same-sounds? [a b]
  ((complement empty?) (some #{a} (combo/permutations b))))

(defn same-sound-entities [sound]
  (let [entities (if (= (count sound) 2) dyad-entities triad-entities)
        entity-components (filter #(same-sounds? sound @(:sound (last %))) entities)]
    (map #(first %) entity-components)))

(defn sound-intersections [ad-entities]
  (apply merge
         (doall
          (for [entity-component ad-entities
                :let [entities (same-sound-entities @(:sound (last entity-component)))
                      perms (combo/permutations entities)]]
            (apply merge (map #(hash-map (first %) (rest %)) perms))))))

(def shared-dyads (sound-intersections dyad-entities))
(def shared-triads (sound-intersections triad-entities))
