(ns move.test
  (:require [move.interactors :as inter]
            [move.models :as models]
            [move.views :as views]
            [goog.dom :as dom])
  (:use-macros [move.macros :only [assert-true with-test-group]]))

(defn test-interactors []
  (with-test-group "interactors"
    [state (models/make-web-state)
     todo (inter/create-new-todo state identity)]
    
    (assert-true (not (nil? todo)))
    (assert-true (= 1 (models/count-lists state)))
    (assert-true (= 1 (models/count-items state "default")))))

(defn test-web-view []
  (with-test-group "webview"
    [content (dom/getElement "content")
     view (views/make-web-view content)
     _ (views/set-items view ["one" "two" "three"])
     _ (views/set-list-name view "sizzle")]

    (assert-true (= 3 (count (.getChildren (:list view)))))
    (assert-true (= "sizzle" (.getText (:list view))))))

;; run the tests
(defn ^:export run []
  (.log js/console "Tests started")
  (test-interactors)
  (test-web-view)
  0)
