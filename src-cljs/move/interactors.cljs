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
  [current-list [get-json "/resources/current-list.json"]
   _ (models/set-current-list state current-list)]
  
  current-list)

(defasync get-list-data [state current-list]
  "[async] get the items in a list from the server and update the model"
  [list-items [get-json "/resources/list-items.json"]
   _ (doseq [item list-items]
       (models/add-todo state current-list item))]

  list-items)

(defasync create-new-todo [state view]
  "[async] create a new item in the current list"
  [list (models/current-list state)
   item (models/add-todo state list "EMPTY")]
  
  (views/set-items view (models/list-items state list))
  item)

(defasync run-application [state view]
  "[async] start the application"
  [current-list [get-current-list state]
   _ (views/set-list-name view current-list)
   list-items [get-list-data state current-list]]

  (views/set-items view list-items)      
  (events/register :create-clicked
                   #(create-new-todo state view identity)))

(comment

  (defn display-active-list [nextfn]
   (models/find-active-list
    (fn [list]
      (let [tree (make-)]))))
  
  (defn focus-for-todo-edit [nextfn todo]
   ))

