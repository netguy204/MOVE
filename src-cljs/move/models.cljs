(ns move.models
  (:require [move.events :as events]
            [move.utils :as utils]))

(defprotocol IDatum
  (value [d])
  (value-key [d]))

(defrecord Datum [the-value the-key]
  IDatum
  (value [d] the-value)
  (value-key [d]
    (when-not @the-key
      (reset! the-key (goog/getUid d)))
    @the-key)

  IHash
  (-hash [d] (value-key d)))

(defprotocol IMarker
  (position [m])
  (move [m p]))

(defrecord Marker [p]
  IMarker
  (position [m] @p)
  (move [m new-p] (reset! p new-p)))

(defn make-marker [p]
  (Marker. (atom p)))

(defn update-marker [m func]
  (move m (func (position m))))

(defn- make-datum [value]
  (Datum. value (atom nil)))

(defprotocol DataModelOperations
  (make-list [state name])

  (add-todo [state list todo position])

  (remove-todo [state list todo])

  (add-marker [state list marker])

  (remove-marker [state list marker])
  
  (all-lists [state])
  
  (list-items [state list])

  (list-markers [state list])

  (current-list [state])

  (set-current-list [state list]))

(defn count-items [state name]
  (count (list-items state name)))

(defn count-lists [state]
  (count (all-lists state)))

(defn- insert [coll val n]
  (concat (take n coll)
          [val]
          (drop n coll)))

(defrecord WebApplicationState [state]

  DataModelOperations
  (make-list [state name]
    (let [datum (make-datum name)]
      (swap! (:state state)
             assoc-in [:lists datum] [])
      datum))

  (add-todo [state list value pos]
    (let [datum (make-datum value)]
      (if-not pos
        (do
          ;; we insert at the beginning and update all of our markers
          (swap! (:state state)
                 update-in [:lists list]
                 conj datum)
          (doseq [m (list-markers state list)]
            (update-marker m inc)))

        (do
          ;; we insert at the requested position and update all
          ;; following markers
          (swap! (:state state)
                 update-in [:lists list]
                 insert datum pos)
          (doseq [m (list-markers state list)]
            (when (>= (position m) pos)
              (update-marker m inc)))))
      
      (events/fire [:add-item list] datum)
      datum))

  (remove-todo [state list todo]
    (let [idx (utils/index-of (list-items state list) todo)]
      (swap! (:state state)
             update-in [:lists list]
             (fn [items] (remove #(= (value-key todo)
                                     (value-key %))
                                 items)))
      (doseq [m (list-markers state list)]
        (when (> (position m) idx)
          (update-marker m dec)))

      (events/fire [:remove-item list] todo)
      idx))

  (add-marker [state list m]
    (swap! (:state state)
           update-in [:markers list]
           conj m))

  (remove-marker [state list m]
    (swap! (:state state)
           update-in [:markers list]
           (fn [markers] (remove #(= m %) markers))))
  
  (all-lists [state]
    (keys (get-in @(:state state) [:lists])))
  
  (list-items [state list]
    (get-in @(:state state) [:lists list]))

  (list-markers [state list]
    (get-in @(:state state) [:markers list]))

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

