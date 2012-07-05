(ns move.events)

(def listeners (atom {}))

(defn- ->coll [val]
  (if (coll? val) val [val]))

(defn register [event callback]
  (swap! listeners update-in [(->coll event)]
         conj callback))

(defn fire [event & args]
  "event is either a single value or a sequence."
  (let [events (->coll event)]
    (doseq [event (reductions conj [] events)]
      (doseq [callback (get-in @listeners [event])]
        (apply callback args)))))

