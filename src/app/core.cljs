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
    {:color (itxt "color")
     :number (itxt "number")}))

(defn parseColorSeqXML [doc]
  (let [label (getElementText doc "/colorseq/label")
        seqid (getAttribute doc "/colorseq" "seqid")
        colorseq (parseElements doc "/colorseq/event" parseEvent)]
    {:label label
     :seqid seqid
     :events colorseq}))

(defn flattenColorEvents [colorSeq]
  (let [expand (fn [event]
                 {:color  (:color  event)
                  :number (:number event)
                  :label  (:label  colorSeq)
                  :seqid  (:seqid  colorSeq)})]
    (map expand (:events colorSeq))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; XhrIo ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn fetch-success [ev ff]
  "handle a successful XhrIo fetch"
  (let []
    (->> ev
         (.-target)
         ;((fn [tgt] (. tgt (getResponseXml))))
         (.getResponseXml)
         (parseColorSeqXML)
         (flattenColorEvents)
         (ff))
    (. js/console (log "fetch success: done"))))


(defn fetch [url ff]
  "send an XhrIo request"
  (let [xhr (gnet/xhr-connection.)]
    (gevent/listen xhr :error #(. js/console (log "fetch error: " %1)))
    (gevent/listen xhr :success #(fetch-success %1 ff))
  (gnet/transmit xhr url "GET" {})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Om UI ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn colorItem [ev]
  "make a <li> from a flattented event"
  (let [color  (:color ev)
        seqid  (:seqid ev)
        label  (:label ev)
        number (:number ev)
        style  #js {:color color}
        text   (string/join " / " [seqid label number color])]
    (dom/li #js {:style style} text)))

(defn widget [data owner]
  (dom/h1 nil "Color events")
  (apply dom/ul nil (map colorItem data)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Run ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(. js/console (log "@@@@@@@@@@@@@@@@@@@@@@@@@"))
(. js/console (log "@@@@@@  XML Demo   @@@@@@"))
(. js/console (log "@@@@@@ version 0.6 @@@@@@"))

(def app-state
  "our app's core state: a list of flattened events"
  ;(atom {:list []})
  (atom [{:seqid "7" :label "ok" :number "21" :color "#720"}]))

(om/root widget app-state
  {:target (. js/document (getElementById "app"))})

; this works
(swap! app-state into
       [{:seqid "8" :label "ok" :number "23" :color "#400"}])

; ahhh, we have to use something like swap! to change atoms
(swap! app-state into
       [{:seqid "9"  :label "whatever" :number "89" :color "#580"}
        {:seqid "10" :label "yay"      :number "7"  :color "#f7f"}])

; using callbacks like this is sketchy
(fetch "/static/alpha.xml" #(swap! app-state into %1))
(fetch "/static/beta.xml"  #(swap! app-state into %1))

; and this is a shoddy way to inspect an atom
(swap! app-state
       (fn [xs]
         (let [first (nth xs 0)
               num   (:number first)]
           (. js/console (log num))
           xs))
       xs)



