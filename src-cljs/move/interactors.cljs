(ns move.interactors
  (:require [move.models :as models]
            [move.views :as views]
            [move.events :as events]
            [move.async :as async]
            [goog.net.XhrIo :as xhr])
  
  (:use-macros [move.macros :only [defasync]]))

(defasync sync-current-list [state]
  "[async] get the current list from the server and update the model"
  [current-list [async/get-json "/resources/current-list.json"]
   list (models/make-list state current-list)]
  
  (models/set-current-list state list)
  list)

(defasync sync-list-data [state list]
  "[async] get the items in a list from the server and update the model"
  [list-items [async/get-json "/resources/list-items.json"]]

  (doall (map #(models/add-todo state list % nil) list-items)))

(defasync create-new-todo [state view]
  "[async] create a new item in the current list"
  [list (models/current-list state)
   value [views/with-dialog-input view "What do you want to do?" nil]]

  (models/add-todo state list value nil))

(defasync edit-todo [state view list-index]
  [list (models/current-list state)
   items (models/list-items state list)
   old-value (nth items list-index)
   new-value [views/with-dialog-input view "Editing item" (models/value old-value)]
   rem-idx (models/remove-todo state list old-value)]

  (models/add-todo state list new-value rem-idx))

(defn sync-view [state view]
  (let [selected-item (atom nil)
        make-view-state
        #(let [current-list (models/current-list state)
               list-items (models/list-items state current-list)]
           {:selected (and @selected-item (models/position @selected-item))
            :list-name (models/value current-list)
            :items (map models/value list-items)})
        sync #(views/set-state view (make-view-state))]
    
    ;; establish event bindings to keep the view current
    (events/register [:change-current-list]
                     (fn [list]
                       (when @selected-item
                         (models/remove-marker state list @selected-item)
                         (reset! selected-item nil))
                       (sync)))
    
    (events/register [:add-item] sync)
    (events/register [:remove-item] sync)

    ;; prevent visual jarring by tracking the selected state locally
    (events/register [:select view]
                     #(if @selected-item
                        (models/move @selected-item %)
                        (let [m (models/make-marker %)]
                          (reset! selected-item m)
                          (models/add-marker state (models/current-list state) m))))

    ;; do the initial sync
    (sync)
    
    ;; could return a registration object here to allow the sync to be
    ;; broken
    ))

(defasync run-application [state view]
  "[async] start the application"
  [current-list [sync-current-list state]
   list-items [sync-list-data state current-list]]

  ;; fill the view with our initial data and keep it syncd
  (sync-view state view)

  ;; listen for events from the view too
  (events/register :create #(create-new-todo state view identity))

  (events/register :clear
                   #(let [current-list (models/current-list state)]
                      (doseq [item (models/list-items state current-list)]
                        (models/remove-todo state current-list item))
                      (views/clear-items view)))

  (events/register :edit #(edit-todo state view % identity)))

(comment

  (defn display-active-list [nextfn]
   (models/find-active-list
    (fn [list]
      (let [tree (make-)]))))
  
  (defn focus-for-todo-edit [nextfn todo]
   ))

