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
  (let [itxt (fn [el] (getSubElementText node el))]
    #{:color (itxt "color")
      :number (itxt "number")}))

(defn parseColorSeqXML [doc]
  (let [label (getElementText doc "/colorseq/label")
        seqid (getAttribute doc "/colorseq" "seqid")
        colorseq (parseElements doc "/colorseq/event" parseEvent)]
    #{:label label
      :seqid seqid
      :events colorseq}))

(defn flattenColorEvents [colorSeq]
  (let [expand (fn [event]
                 #{:color (:color event)
                   :number (:number event)
                   :label (:label colorSeq)
                   :seqid (:seqid colorSeq)})]
    (map expand (:events colorSeq))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; XhrIo ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn fetch-success [ev]
  "handle a successful XhrIo fetch"
  (let [tgt (.-target ev)
        xml (. tgt (getResponseXml))
        txt (. xml (toString))
        cs  (parseColorSeqXML xml)]
    (. js/console (log "fetch success"))
    ;(. js/console (log "ev: " ev))
    ;(. js/console (log "tgt: " tgt))
    ;(. js/console (log "xml: " xml))
    ;(. js/console (log "txt: " txt))
    (. js/console (log "color seq: " cs))))


(defn fetch [url]
  "send an XhrIo request"
  (let [xhr (gnet/xhr-connection.)]
    (gevent/listen xhr :error #(. js/console (log "fetch error: " %1)))
    (gevent/listen xhr :success fetch-success)
  (gnet/transmit xhr url "GET" {})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Om UI ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def app-state (atom {:list [#{:seqid "7"
                               :label "ok"
                               :number "21"
                               :color "#720"}]}))

(defn colorEventItem [ev]
  (let [color (:color ev)]
    (dom/li #js{:style (:color color)}
            "tmp2"
            (comment str (:seqid ev) " / "
                 (:label ev) " / "
                 (:number ev) " / "
                 (color))))
    (comment dom/li nil "tmp"))


(defn widget [data owner]
  (dom/h1 nil "Color events")
  (apply dom/ul nil
         (map colorEventItem (:list data))))

(om/root
  widget
  app-state
  {:target (. js/document (getElementById "app"))})


(fetch "/static/alpha.xml")
(fetch "/static/beta.xml")

;;(swap! app-state assoc :text "Hmmm.")


(. js/console (log "@@@@@@@@@@@@@@@@@@@@@@@@@"))
(. js/console (log "@@@@@@  XML Demo   @@@@@@"))
(. js/console (log "@@@@@@ version 0.1 @@@@@@"))


