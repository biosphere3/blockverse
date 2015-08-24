(ns ^:figwheel-always designer.core
  (:require [om.core :as om :include-macros true ]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [om.dom :as dom :include-macros true]
            ))

(enable-console-print!)

(defn log [x] (.log js/console x))

(defrecord Port [type name scalar units])

#_(defn mkport [type name scalar units]
  {:type type
   :name name
   :booboo {:boo :boo}
   :scalar scalar
   :units units})

(def initial-state
  {:text "Hello world!!"
   :blocks [{:name "humanoid"
             :ports [(->Port :input "food" 360 "pounds per year")
                     (->Port :input "water" 2 "L per day")]}]})

(def app-state (atom initial-state))

(defn simple-html-view [markup-fn]
  (fn [cursor owner]
    (reify
      om/IRender
      (render [this] (html (markup-fn cursor owner))))))

(defcomponent editable-field [data owner {:keys [field]}]
  (init-state [this] {:editing false})
  (render [this]
    (let [handle-edit (fn [e]
                        (om/update! data field (.. e -target -value)))]
      (log owner)
      (log (:editing owner))
      (if true
        (html
          [:input {:value (field data)
                   :onChange handle-edit}])
        (html
          [:span {:onDoubleClick #(om/set-state! owner :editing true)} (field data)])))))

(defcomponent port-view [port owner]
  (render [this]
          (html
            [:li.port
             [:select [:option "input"] [:option "output"]]
             (om/build editable-field port {:opts {:field :name}})
             (om/build editable-field port {:opts {:field :scalar}})
             (om/build editable-field port {:opts {:field :units}})
             #_[:input {:value (:name port)
                      :onChange (handle-edit :name)
                      :placeholder "name"}]
             #_[:input {:value (:scalar port)}]
             #_[:input {:value (:units port)}]
             ])))

(defcomponent block-view [block owner]
  (render [this]
          (html
            [:li.block
             [:label "name " [:input {:value (:name block)}]]
             [:ul.ports
              (om/build-all port-view (:ports block))
              ]])))

(om/root
  (fn [data owner]
    (reify om/IRender
      (render [_]
              (html [:.form [:h1 "DESIGNER"]
                     [:ul
                      [:h2 "blocks"]
                      (om/build-all block-view (:blocks data))]])
              )))
  app-state
  {:target (. js/document (getElementById "app"))})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
  )

