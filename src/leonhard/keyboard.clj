(def ^:const FIFTEEN-DEG (/ (Math/PI) 12))
(def ^:const THIRTY-DEG (* 2 FIFTEEN-DEG))
(def ^:const FORTY-FIVE-DEG (* 3 FIFTEEN-DEG))
(def ^:const SIXTY-DEG (* 2 THIRTY-DEG))

(def ^:const FULL-LENGTH 100)
(def ^:const HALF-LENGTH (/ FULL-LENGTH 2))
(def ^:const VERTEX-TO-CENTER (/ FULL-LENGTH 2 (Math/cos THIRTY-DEG)))
(def ^:const SIDE-TO-CENTER (* FULL-LENGTH 0.5 (Math/tan THIRTY-DEG)))

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
  (q/vertex (negate HALF-LENGTH) HALF-LENGTH)
  (q/vertex HALF-LENGTH HALF-LENGTH)
  (q/vertex HALF-LENGTH (negate HALF-LENGTH))
  (q/vertex (negate HALF-LENGTH) (negate HALF-LENGTH))
  (q/end-shape :close))

(defn triangle-key []
  (q/begin-shape)
  (q/vertex (negate HALF-LENGTH) (negate SIDE-TO-CENTER))
  (q/vertex HALF-LENGTH (negate SIDE-TO-CENTER))
  (q/vertex 0 VERTEX-TO-CENTER)
  (q/end-shape :close))
