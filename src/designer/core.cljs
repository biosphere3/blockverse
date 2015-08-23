(ns ^:figwheel-always designer.core
  (:require [om.core :as om :include-macros true ]
            [sablono.core :as html :refer-macros [html]]
            [om.dom :as dom :include-macros true]
            ))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

(defrecord Port [type name scalar units])

(def initial-state
  {:text "Hello world!!"
   :blocks [{:name "humanoid"
             :ports [(->Port :input "food" 360 "pounds per year")
                     (->Port :input "water" 2 "L per day")]}]})

(println "INITIAL STATE:" initial-state)

(def app-state (atom initial-state))

(defn html-port [port]
  [:.port
   [:span "name "]
   [:select [:option "input"] [:option "output"]]
   [:input {:value (:name port)}]
   [:input {:value (:scalar port)}]
   [:input {:value (:units port)}]
   ])

(defn html-blocks [blocks]
  [:ul.blocks
   [:h2 "blocks"]
   (for [block blocks]
     [:li
      [:label "name " [:input {:value (:name block)}]]
      [:ul.ports
       [:h2 "ports "]
       (for [port (:ports block)]
         (html-port port))]])])

(om/root
  (fn [data owner]
    (reify om/IRender
      (render [_]
              (html [:.form [:h1 "DESIGNER"]
                     (html-blocks (:blocks data))])
              )))
  app-state
  {:target (. js/document (getElementById "app"))})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
  )

