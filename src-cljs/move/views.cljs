(ns move.views
  (:require [goog.ui.tree.TreeControl :as TreeControl]
            [goog.ui.Button :as Button]
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

  (set-list-name [view name]))

(defn- make-list-view [name]
  (let [config goog/ui.tree.TreeControl.defaultConfig]
    (goog/ui.tree.TreeControl. name config)))

(defn- clear-list-view [view]
  (doseq [child (.getChildren view)]
    (.removeChild view child)))

(defn- extend-list-view [view items]
  (doseq [item items]
    (let [subnode (.createNode view (render item))]
      (.setClientData subnode item)
      (.add view subnode))))

(defrecord WebTodoView [list add-button]
  ViewOperations

  (set-items [view items]
    (clear-list-view (:list view))
    (extend-list-view (:list view) items))

  (set-list-name [view name]
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
    view))

(defn make-noop-view []
  (reify ViewOperations
    (set-items [view items] true)
    (set-list-name [view name] true)))
