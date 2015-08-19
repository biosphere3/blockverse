(ns ^:figwheel-always designer.core
  (:require [om.core :as om :include-macros true ]
            [sablono.core :as html :refer-macros [html]]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

(def initial-state
  {:text "Hello world!!"
   :blocks [:a :b]})

(println "INITIAL STATE:" initial-state)

(defonce app-state (atom initial-state))

(defn html-blocks [blocks]
  (println blocks)
  [:ul.blocks
   (for [block blocks]
     [:li
      [:input {:value "hi"}]])])

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

