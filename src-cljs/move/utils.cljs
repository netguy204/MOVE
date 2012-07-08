(ns move.utils)

(defn indices-of [coll value]
  (let [enumerated (map-indexed vector coll)]
    (map first (filter #(= value (second %)) enumerated))))

(defn index-of [coll value]
  (first (indices-of coll value)))
