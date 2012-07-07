(ns move.events)

(def listeners (atom {}))
(def ^:dynamic *current-event* nil)

(defn- ->coll [val]
  (if (coll? val) val [val]))

(defn register [event callback & curried]
  (let [binding {:callback callback
                 :curried curried}]
    (swap! listeners update-in [(->coll event)]
           conj binding)
    binding))

(defn unregister [event binding]
  (swap! listeners update-in [(->coll event)]
         (fn [bindings]
           (remove #(identical? binding %) bindings))))

(defn fire [event & args]
  "event is either a single value or a sequence."
  (binding [*current-event* event]
    (let [events (->coll event)]
      (doseq [event (reductions conj [] events)]
        (doseq [{callback :callback
                 curried :curried} (get-in @listeners [event])]
          (apply callback (concat curried args)))))))
