(ns ^:figwheel-always designer.core
  (:require [om.core :as om :include-macros true ]
            [om-tools.core :refer-macros [defcomponent]]
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

(defn simple-html-view [markup-fn]
  (fn [cursor owner]
    (reify
      om/IRender
      (render [this] (html (markup-fn cursor owner))))))


(defcomponent port-view [port owner]
  (render [this]
          (html
            [:.port
             [:span "name "]
             [:select [:option "input"] [:option "output"]]
             [:input {:value (:name port)}]
             [:input {:value (:scalar port)}]
             [:input {:value (:units port)}]
             ])))

(defcomponent block-view [block owner]
  (render [this]
          (html
            [:li
             [:label "name " [:input {:value (:name block)}]]
             [:ul.ports
              [:h2 "ports "]
              (om/build-all port-view (:ports block))
              #_(for [port (:ports block)]
                  (html-port port))]])))

(om/root
  (fn [data owner]
    (reify om/IRender
      (render [_]
              (html [:.form [:h1 "DESIGNER"]
                     [:ul "blocks"
                      (om/build-all block-view (:blocks data))]])
              )))
  app-state
  {:target (. js/document (getElementById "app"))})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
  )

