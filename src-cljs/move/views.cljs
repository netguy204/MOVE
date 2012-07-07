(ns move.views
  (:require [goog.ui.tree.TreeControl :as TreeControl]
            [goog.ui.Button :as Button]
            [goog.ui.Dialog :as Dialog]
            [goog.ui.LabelInput :as LabelInput]
            [goog.dom :as dom]
            [goog.events :as ge]
            [move.events :as events]))

(defn content-view []
  (dom/getElement "content"))

;; something that knows how to turn itself into HTML
(defprotocol Renderable
  (render [item]))

;; strings become HTML directly
(extend-type string

  Renderable
  (render [item] item))

(extend-type cljs.core.keyword

  Renderable
  (render [item] (name item)))

(extend-type default
  cljs.core.IHash
  (-hash [o] (goog/getUid o)))

(defprotocol ViewOperations
  (set-items [view items])

  (append-item [view item])

  (clear-items [view])
  
  (set-name [view name]))

(defn- make-list-view [name]
  (let [config goog/ui.tree.TreeControl.defaultConfig]
    (goog/ui.tree.TreeControl. name config)))

(defn- clear-list-view [view]
  (doseq [child (.getChildren view)]
    (.removeChild view child)))

(defn- append-list-item [view item]
  (let [subnode (.createNode view (render item))]
    (.setClientData subnode item)
    (.add view subnode)))

(defn- extend-list-items [view items]
  (doseq [item items]
    (append-list-item view item)))

(defn- get-list-name [view]
  (.getHtml (:list view)))

(defrecord WebTodoView [list add-button]
  ViewOperations

  (set-items [view items]
    (clear-list-view (:list view))
    (extend-list-items (:list view) items))

  (append-item [view item]
    (append-list-item (:list view) item))

  (clear-items [view]
    (clear-list-view (:list view)))
  
  (set-name [view name]
    (.setHtml (:list view) (render name))))

(defn make-web-view [el]
  (let [list (make-list-view "[]")
        create-button (goog/ui.Button. "Create")
        clear-button (goog/ui.Button. "Clear")
        view (WebTodoView. list create-button)]
    (ge/listen create-button "action" #(events/fire [:create-clicked view]))
    (ge/listen clear-button "action" #(events/fire [:clear-clicked view]))
    
    (.render list el)
    (.render create-button el)
    (.render clear-button el)

    view))

(defn make-noop-view []
  (reify ViewOperations
    (set-items [view items] true)
    (append-item [view item] true)
    (clear-items [view] true)
    (set-name [view name] true)))


(defn make-input-dialog [message]
  (let [dialog (goog/ui.Dialog.)
        input (goog/ui.LabelInput. "type here")]
    (.setTitle dialog message)
    (.addChild dialog input true)
    (.setVisible dialog true)
    (.focusAndSelect input)
    
    (ge/listenOnce dialog (.-SELECT goog/ui.Dialog.EventType)
               #(when (= (.-key %) (.-OK goog/ui.Dialog.DefaultButtonKeys))
                  (events/fire [:ok-clicked dialog] (.getValue input))))
    
    dialog))

(defn with-input [view callback]
  (events/register-once [:ok-clicked view] callback))
