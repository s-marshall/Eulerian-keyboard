(def ^:const FIFTEEN-DEG (/ (Math/PI) 12))
(def ^:const THIRTY-DEG (* 2 FIFTEEN-DEG))
(def ^:const FORTY-FIVE-DEG (* 3 FIFTEEN-DEG))
(def ^:const SIXTY-DEG (* 2 THIRTY-DEG))

(def ^:const FULL-LENGTH 100)
(def ^:const HALF-LENGTH (/ FULL-LENGTH 2))

(defn negate [x] (- 0 x))

(defn hexagonal-key []
  (let [x (* FULL-LENGTH (Math/cos THIRTY-DEG))
        negative-x (negate x)]
  (q/begin-shape)
  (q/vertex x HALF-LENGTH)
  (q/vertex 0 FULL-LENGTH)
  (q/vertex negative-x HALF-LENGTH)
  (q/vertex negative-x (negate HALF-LENGTH))
  (q/vertex 0 (negate FULL-LENGTH))
  (q/vertex x (negate HALF-LENGTH))
  (q/end-shape :close)))

(defn square-key []
  (q/begin-shape)
  (q/vertex HALF-LENGTH (negate HALF-LENGTH))
  (q/vertex HALF-LENGTH HALF-LENGTH)
  (q/vertex (negate HALF-LENGTH) HALF-LENGTH)
  (q/vertex (negate HALF-LENGTH) (negate HALF-LENGTH))
  (q/end-shape :close))

(defn triangle-key []
  (let [delta (* HALF-LENGTH (Math/cos THIRTY-DEG) (Math/cos FORTY-FIVE-DEG))]
    (q/begin-shape)
    (q/vertex 0 0)
    (q/vertex (* FULL-LENGTH (Math/sin THIRTY-DEG)) (* FULL-LENGTH (Math/cos THIRTY-DEG)))
    (q/vertex FULL-LENGTH 0)
    (q/end-shape :close)))
