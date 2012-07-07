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

(defn register-once [event callback & curried]
  (let [binding (atom nil)
        new-callback (fn [& args]
                       (unregister event @binding)
                       (apply callback args))]
    (reset! binding (apply register event new-callback curried))
    @binding))

(defn fire [event & args]
  "event is either a single value or a sequence."
  (binding [*current-event* event]
    (let [events (->coll event)]
      (doseq [event (reductions conj [] events)]
        (doseq [{callback :callback
                 curried :curried} (get-in @listeners [event])]
          (apply callback (concat curried args)))))))
