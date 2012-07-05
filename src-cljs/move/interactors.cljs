(ns move.interactors
  (:require [move.models :as models]
            [move.views :as views]))

(defn create-new-todo [state view nextfn]
  (let [list (models/current-list state)
        item (models/add-todo state list "EMPTY")]
    (views/set-items view (models/list-items state list))
    (nextfn state view item)))

(comment

  (defn display-active-list [nextfn]
   (models/find-active-list
    (fn [list]
      (let [tree (make-)]))))
  
  (defn focus-for-todo-edit [nextfn todo]
   ))

