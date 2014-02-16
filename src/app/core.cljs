(ns xmldemo.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [goog.dom.xml :as xml]
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
  (let [tgt (.-target ev)
        xml (. tgt (getResponseXml))
        txt (. xml (toString))
        cs  (parseColorSeqXML xml)]
    (. js/console (log "fetch success"))
    (. js/console (log "ev: " ev))
    (. js/console (log "tgt: " tgt))
    (. js/console (log "xml: " xml))
    (. js/console (log "txt: " txt))
    (. js/console (log "color seq: " cs))))


(defn fetch [url]
  (let [xhr (gnet/xhr-connection.)]
    (gevent/listen xhr :error
                   #(. js/console (log "fetch error: " %1)))
    (gevent/listen xhr :success fetch-success)

  (gnet/transmit xhr url "GET" {})))



(fetch "/static/alpha.xml")
(fetch "/static/beta.xml")
;; (fetch "/static/schema.xml")


(defn getSubElementText [element subElementName]
  (let [matches (. element (getElementsByTagName subElementName))
        match (aget matches 0)
        txt (.-innerHTML match)]
    txt))

(defn getElementText [doc path]
  (let [elem (xml/selectSingleNode doc path)]
    (.-innerHTML elem)))

(defn getAttribute [doc path attribute]
  (let [elem (xml/selectSingleNode doc path)]
    (. elem (getAttribute attribute))))

(defn parseElements [doc pattern ff]
  (let [nodes (xml/selectNodes doc pattern)]
    (map ff nodes)))

(defn parseEvent [node]
  (let [itxt (getSubElementText node)]
    #{:color (itxt "color")
      :number (itxt "number")}))

(defn parseColorSeqXML [doc]
  (let [label (getElementText doc "/colorseq/label")
        seqid (getAttribute doc "/colorseq" "seqid")
        colorseq (parseElements doc "/colorseq/event" parseEvent)]
    #{:label label
      :seqid seqid
      :events colorseq}))









