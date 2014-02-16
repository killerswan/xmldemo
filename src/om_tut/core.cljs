(ns om-tut.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [clojure.data :as data]
            [clojure.string :as string]
            [clojure.browser.event :as gevent]
            [clojure.browser.net :as gnet]))


(enable-console-print!)

(def app-state (atom {:text "Hello world!"}))

(om/root
  app-state
  (fn [app owner]
    (dom/h1 nil (:text app)))
  (. js/document (getElementById "app")))

(. js/console (log "Hello world!"))

(defn parse-contact [contact-str]
  (let [[first middle last :as parts] (string/split contact-str #"\s+")
        [first last middle] (if (nil? last) [first middle] [first last middle])
        middle (when middle (string/replace middle "." ""))
        c (if middle (count middle) 0)]
    (when (>= (reduce + (map #(if % 1 0) parts)) 2)
      (cond-> {:first first :last last}
              (== c 1) (assoc :middle-initial middle)
              (>= c 2) (assoc :middle middle)))))


(defn fetch-success [ev]
  (. js/console (log "fetch success: " ev)))

(defn fetch [url]
  (let [xhr (gnet/xhr-connection.)]
    (gevent/listen xhr :error
                   #(. js/console (log "fetch error: " %1)))
    (gevent/listen xhr :success fetch-success)

  (gnet/transmit xhr url "GET" {})))



(fetch "/static/alpha.xml")










