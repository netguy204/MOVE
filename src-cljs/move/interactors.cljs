(ns move.interactors
  (:require [move.models :as models]))

(defn create-new-todo [state nextfn]
  (let [list (models/current-list state)]
    (nextfn state (models/add-todo state list "EMPTY"))))

(comment

  (defn display-active-list [nextfn]
   (models/find-active-list
    (fn [list]
      (let [tree (make-)]))))
  
  (defn focus-for-todo-edit [nextfn todo]
   ))

