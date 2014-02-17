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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; XML parsing ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn getSubElementText [element subElementName]
  "get text out of a subelement, e.g., <elem><subelem>text</..."
  (let [matches (. element (getElementsByTagName subElementName))
        match (aget matches 0)
        txt (.-innerHTML match)]
    txt))

(defn getElementText [doc path]
  "get text out of an element, e.g., <elem>text</..."
  (let [elem (xml/selectSingleNode doc path)]
    (.-innerHTML elem)))

(defn getAttribute [doc path attribute]
  "get text out of an attribute, e.g., <elem attr='text'>..."
  (let [elem (xml/selectSingleNode doc path)]
    (. elem (getAttribute attribute))))

(defn parseElements [doc pattern ff]
  "map a function over elements matching a pattern"
  (let [nodes (xml/selectNodes doc pattern)]
    (map ff nodes)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; XML parsing (specific) ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn parseEvent [node]
  "get a color,number pair out of an element"
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; XhrIo ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn fetch-success [ev]
  "handle a successful XhrIo fetch"
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
  "send an XhrIo request"
  (let [xhr (gnet/xhr-connection.)]
    (gevent/listen xhr :error #(. js/console (log "fetch error: " %1)))
    (gevent/listen xhr :success fetch-success)
  (gnet/transmit xhr url "GET" {})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Om UI ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def app-state (atom {:text "Hello world!"}))

(defn parse-contact [contact-str]
  (let [[first middle last :as parts] (string/split contact-str #"\s+")
        [first last middle] (if (nil? last) [first middle] [first last middle])
        middle (when middle (string/replace middle "." ""))
        c (if middle (count middle) 0)]
    (when (>= (reduce + (map #(if % 1 0) parts)) 2)
      (cond-> {:first first :last last}
              (== c 1) (assoc :middle-initial middle)
              (>= c 2) (assoc :middle middle)))))



(om/root
  app-state
  (fn [app owner]
    (dom/h1 nil (:text app)))
  (. js/document (getElementById "app")))

(. js/console (log "Hello world!"))

(fetch "/static/alpha.xml")
(fetch "/static/beta.xml")





