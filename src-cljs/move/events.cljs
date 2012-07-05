(ns move.events)

(def listeners (atom {}))

(defn register [event callback]
  (swap! listeners update-in [event]
         conj callback))

(defn fire [event & args]
  (doseq [callback (get-in @listeners [event])]
    (apply callback args)))

