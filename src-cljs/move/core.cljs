(ns move.core
  (:require [move.models :as models]
            [move.interactors :as inter]
            [move.views :as views]
            [goog.dom :as dom]))


(defn- content []
  (dom/getElement "content"))

(defn ^:export main []
  (let [content (content)
        view (views/make-web-view content)
        state (models/make-web-state)
        list "Shopping List"]

    (models/make-list state list)
    (models/add-todo state list "Potatos")
    (models/add-todo state list "Carrots")
    (models/add-todo state list "Peas")

    (views/set-items view (models/list-items state list))
    (views/set-list-name view list)))


