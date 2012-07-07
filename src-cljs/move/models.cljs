(ns move.models
  (:require [move.events :as events]))

(defprotocol DataModelOperations
  (make-list [state name])

  (add-todo [state list value])

  (remove-todo [state list value])

  (all-lists [state])
  
  (list-items [state list])

  (current-list [state])

  (set-current-list [state list]))

(defn count-items [state name]
  (count (list-items state name)))

(defn count-lists [state]
  (count (all-lists state)))


(defrecord WebApplicationState [state]

  DataModelOperations
  (make-list [state name]
    (swap! (:state state)
           assoc-in [:lists name] [])
    name)

  (add-todo [state name value]
    (swap! (:state state)
           update-in [:lists name]
           conj value)
    (events/fire [:add-item name] value))

  (remove-todo [state name value]
    (swap! (:state state)
           update-in [:lists name]
           (fn [items] (remove #(identical? value %) items))))

  (all-lists [state]
    (keys (get-in @(:state state) [:lists])))
  
  (list-items [state name]
    (get-in @(:state state) [:lists name]))

  (current-list [state]
    (if-let [current (get-in @(:state state) [:current-list])]
      current
      (do
        (let [current (make-list state "default")]
          (set-current-list state current)
          current))))

  (set-current-list [state list]
    (swap! (:state state) assoc-in [:current-list] list)
    (events/fire [:change-current-list] list)))

(defn make-web-state []
  (WebApplicationState. (atom {})))

