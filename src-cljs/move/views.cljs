(ns move.views
  (:require [goog.ui.tree.TreeControl :as TreeControl]
            [goog.ui.Button :as Button]
            [goog.ui.Dialog :as Dialog]
            [goog.ui.LabelInput :as LabelInput]
            [goog.dom :as dom]
            [goog.events :as ge]
            [move.events :as events]
            [move.utils :as utils]))

(defn content-view []
  (dom/getElement "content"))

(extend-type default
  IHash
  (-hash [o] (goog/getUid o)))

(defprotocol ViewOperations
  (set-state [view view-state])
  (with-dialog-input [view message default callback]))

(defn- make-list-view [name]
  (let [config goog/ui.tree.TreeControl.defaultConfig]
    (goog/ui.tree.TreeControl. name config)))

(defn- get-children [view]
  (or (.getChildren view) nil))

(defn- clear-list-view [view]
  (doseq [child (get-children view)]
    (.removeChild view child)))

(defn- append-list-item [view item]
  (let [subnode (.createNode view item)]
    (.setClientData subnode item)
    (.add view subnode)))

(defn- extend-list-items [view items]
  (doseq [item items]
    (append-list-item view item)))

(defn- set-list-name [view name]
  (.setHtml view name))

(defn- get-selected-index [view]
  (let [entry (.getSelectedItem view)]
    (utils/index-of (get-children view) (.getSelectedItem view))))

(defn- set-selected-index [view idx]
  (let [entry (nth (get-children view) idx)]
    (.setSelectedItem view entry)))

;; view state
;;
;; {:selected 3
;;  :list-name "Groceries"
;;  :items ["Grapes"
;;          "Fuel"]}

(defn- make-input-dialog [message default]
  (let [dialog (goog/ui.Dialog.)
        input (goog/ui.LabelInput. "Pick up milk")]
    (.setTitle dialog message)
    (.addChild dialog input true)
    (.setVisible dialog true)

    (set! (.-size (.getElement input)) 50)
    (when default
      (.setValue input default))
    (.focusAndSelect input)

    (ge/listenOnce dialog (.-SELECT goog/ui.Dialog.EventType)
               #(when (= (.-key %) (.-OK goog/ui.Dialog.DefaultButtonKeys))
                  (events/fire [:value-produced dialog] (.getValue input))))
    
    dialog))

(defn- with-value [view callback]
  (events/register-once [:value-produced view] callback))

(defn- with-web-dialog-input [message default callback]
  (let [dialog (make-input-dialog message default)]
    (with-value dialog callback)))

(defrecord WebTodoView [list state]
  ViewOperations
  (set-state [view new-state]
    (clear-list-view (:list view))
    (set-list-name (:list view) (:list-name new-state))
    (extend-list-items (:list view) (:items new-state))
    (when (:selected new-state)
      (set-selected-index (:list view) (:selected new-state))))

  (with-dialog-input [view message default callback]
    (with-web-dialog-input message default callback)))

(defn make-web-view [el]
  (let [list (make-list-view "[]")
        create-button (goog/ui.Button. "Create")
        clear-button (goog/ui.Button. "Clear")
        view (WebTodoView. list)]

    (.render list el)
    (.render create-button el)
    (.render clear-button el)

    (ge/listen create-button "action" #(events/fire [:create view]))
    (ge/listen clear-button "action" #(events/fire [:clear view]))
    (ge/listen (.getElement list) (.-DBLCLICK goog/events.EventType)
               #(events/fire [:edit view] (get-selected-index list)))
    (ge/listen (.getElement list) (.-CLICK goog/events.EventType)
               #(events/fire [:select view] (get-selected-index list)))
    
    view))

(defn make-noop-view [test-input]
  (reify ViewOperations
    (set-state [view state] nil)

    (with-dialog-input [view message default callback]
      (callback test-input))))


