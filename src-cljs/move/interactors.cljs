(ns move.interactors
  (:require [move.models :as models]
            [move.views :as views]
            [move.events :as events]
            [goog.net.XhrIo :as xhr]
            [goog.json :as json])
  
  (:use-macros [move.macros :only [doasync]]))

(defn- ev->str [ev]
  "convert a xhr event object into the text it contains"
  (.getResponseText (.-target ev)))

(defn get-json [url nextfn]
  "[async] retrieve the data at url"
  (xhr/send url #(nextfn (json/unsafeParse (ev->str %)))))

(defn get-current-list [state nextfn]
  "[async] get the current list from the server and update the model"
  (doasync
   [current-list [get-json "/resources/current-list.json"]
    _ (models/set-current-list state current-list)
    _ (nextfn current-list)]))

(defn get-list-data [state current-list nextfn]
  "[async] get the items in a list from the server and update the model"
  (doasync
   [list-items [get-json "/resources/list-items.json"]
    _ (doseq [item list-items]
        (models/add-todo state current-list item))
    _ (nextfn list-items)]))

(defn create-new-todo [state view nextfn]
  "[async] create a new item in the current list"
  (let [list (models/current-list state)
        item (models/add-todo state list "EMPTY")]
    (views/set-items view (models/list-items state list))
    (nextfn state view item)))

(defn run-application [state view nextfn]
  "[async] start the application"
  (doasync
   [current-list [get-current-list state]
    _ (views/set-list-name view current-list)
    list-items [get-list-data state current-list]
    _ (views/set-items view list-items)])

      
  (events/register :create-clicked
                   #(create-new-todo state view identity)))

(comment

  (defn display-active-list [nextfn]
   (models/find-active-list
    (fn [list]
      (let [tree (make-)]))))
  
  (defn focus-for-todo-edit [nextfn todo]
   ))

