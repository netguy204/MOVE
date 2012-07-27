(ns move.async
  (:use-macros [move.macros :only [defasync doasync]])
  (:use [cljs.reader :only [read-string]])
  (:require [goog.net.XhrIo :as xhr]
            [goog.json :as json]))

;; borrowed from ibdknox/jayq
(defn- map->js [m]
  (let [out (js-obj)]
    (doseq [[k v] m]
      (aset out (name k) v))
    out))

(defn clj->js
  "Recursively transforms ClojureScript maps into Javascript objects,
   other ClojureScript colls into JavaScript arrays, and ClojureScript
   keywords into JavaScript strings."
  [x]
  (cond
    (string? x) x
    (keyword? x) (name x)
    (map? x) (.-strobj (reduce (fn [m [k v]]
                                 (assoc m (clj->js k) (clj->js v))) {} x))
    (coll? x) (apply array (map clj->js x))
    :else x))
;; end jayq

(defn- ev->str [ev]
  "convert a xhr event object into the text it contains"
  (.getResponseText (.-target ev)))

(defasync get-json [url]
  "[async] retrieve the data at url"
  [event [xhr/send url]]

  (json/unsafeParse (ev->str event)))

(defasync get-clj [url]
  [event [xhr/send url]]

  (read-string (ev->str event)))

(defn- post-xhr [url data callback]
  (xhr/send url callback "POST" data (clj->js {"Content-Type" "text/text"})))

(defasync post-clj [url data]
  [data (pr-str data)
   result [post-xhr url data]]

  (read-string (ev->str result)))

