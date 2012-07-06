(ns move.core
  (:require [move.models :as models]
            [move.interactors :as inter]
            [move.views :as views]
            [move.events :as events]
            [goog.dom :as dom]))


(defn- content []
  (dom/getElement "content"))

(defn ^:export main []
  (let [content (content)
        view (views/make-web-view content)
        state (models/make-web-state)
        list "Shopping List"]

    ;; start the application
    (inter/run-application state view identity)))


