(ns move.interactors
  (:require [move.models :as models]
            [move.views :as views]
            [move.events :as events]
            [goog.net.XhrIo :as xhr]
            [goog.json :as json])
  
  (:use-macros [move.macros :only [defasync]]))

(defn- ev->str [ev]
  "convert a xhr event object into the text it contains"
  (.getResponseText (.-target ev)))

(defasync get-json [url]
  "[async] retrieve the data at url"
  [event [xhr/send url]]

  (json/unsafeParse (ev->str event)))

(defasync get-current-list [state]
  "[async] get the current list from the server and update the model"
  [current-list [get-json "/resources/current-list.json"]]
  
  (models/set-current-list state current-list)
  current-list)

(defasync get-list-data [state current-list]
  "[async] get the items in a list from the server and update the model"
  [list-items [get-json "/resources/list-items.json"]]

  (doseq [item list-items]
    (models/add-todo state current-list item))
  
  list-items)

(defasync create-new-todo [state view]
  "[async] create a new item in the current list"
  [list (models/current-list state)
   input-dialog (views/make-input-dialog "What do you want to do?")
   input [views/with-input input-dialog]]

  (models/add-todo state list input))

(defasync run-application [state view]
  "[async] start the application"
  [current-list [get-current-list state]
   list-items [get-list-data state current-list]]

  ;; fill the view with our initial data
  (views/set-name view current-list)
  (views/set-items view list-items)

  ;; establish event bindings to keep the view current
  (events/register [:change-current-list]
                   (fn [name]
                     (views/set-name view name)
                     (views/set-items view (models/list-items state name))))

  (events/register [:add-item] #(views/append-item view %))

  ;; listen for events from the view too
  (events/register :create-clicked
                   #(create-new-todo state view identity))

  (events/register :clear-clicked
                   #(let [current-list (models/current-list state)]
                      (doseq [item (models/list-items state current-list)]
                        (models/remove-todo state current-list item))
                      (views/clear-items view))))

(comment

  (defn display-active-list [nextfn]
   (models/find-active-list
    (fn [list]
      (let [tree (make-)]))))
  
  (defn focus-for-todo-edit [nextfn todo]
   ))

