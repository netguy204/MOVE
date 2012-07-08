(ns move.test
  (:require [move.interactors :as inter]
            [move.models :as models]
            [move.views :as views]
            [move.events :as events]
            [goog.dom :as dom])
  
  (:use-macros [move.macros :only [assert-true assert-false with-test-group]]))

;; stuff
(defn test-interactors []
  (with-test-group "interactors"
    [state (models/make-web-state)
     list (models/make-list state "test")
     _ (models/set-current-list state list)
     test-input "test input"
     view (views/make-noop-view test-input)
     todo (inter/create-new-todo state view identity)]
    
    (assert-true (not (nil? todo)))
    (assert-true (= test-input (value todo)))
    (assert-true (= 1 (models/count-lists state)))
    (assert-true (= 1 (models/count-items state list)))))

(defn test-web-view []
  (with-test-group "webview"
    [content (dom/getElement "content")
     view (views/make-web-view content)
     _ (views/set-state view {:selected 1
                              :list-name "sizzle"
                              :items ["one" "two" "three"]})]
    
    (assert-true (= 3 (count (.getChildren (:list view)))))
    (assert-true (= "sizzle" (.getText (:list view))))))

(defn test-events []
  (with-test-group "events"
    [a (atom nil)
     b (atom nil)
     all (atom nil)
     _ (events/register [] #(reset! all %))]

    (assert-false @a)
    (assert-false @all)
    
    (assert-true
     (do
       (events/register :foo #(reset! a %))
       (events/fire :foo true)
       @a))
    (assert-true @all)
    
    (assert-false @b)

    (assert-true
     (do
       (events/register :bar #(reset! b %))
       (events/fire :bar true)
       @b))
    (assert-true @all)

    (assert-false (do (events/fire :foo false) @a))
    (assert-false @all)))

(defn ^:export run []
  (.log js/console "Tests started")
  (test-interactors)
  (test-web-view)
  (test-events)
  0)
