(ns ^:figwheel-always designer.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [cljs.pprint :refer (pprint)]
            [cljs.reader :refer (read-string)]
            [cljs.core.async :refer [put! chan <!]]
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
  {:blocks [{:name "humanoid"
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
      (if true
        (html
          [:input {:value (field data)
                   :onChange handle-edit}])
        (html
          [:span {:onDoubleClick #(om/set-state! owner :editing true)} (field data)])))))

(defn edit-fn [data field f]
  (fn [e] (om/update! data field (f (.. e -target -value)))))

(defcomponent port-view [port owner]
  (render-state [this {:keys [comm]}]
          (html
            [:li.port
             [:button {:onClick #(put! comm [:delete-port port])}]
             [:select [:option "input"] [:option "output"]]
             [:input {:value (:name port)
                      :onChange (edit-fn port :name str)
                      :placeholder "name"}]
             [:input {:value (:scalar port)
                      :onChange (edit-fn port :scalar read-string)}]  ; TODO use something better than read-string
             [:input {:value (:units port)
                      :onChange (edit-fn port :units str)}]
             ])))

(defcomponent block-view [block owner]
  (render-state [this {:keys [comm]}]
          (html
            [:li.block
             [:button {:onClick #(put! comm [:delete-block @block])} "×"]
             [:label "name " [:input {:value (:name block)
                                      :onChange (edit-fn block :name str)}]]
             [:ul.port-list
              (om/build-all port-view (:ports block) {:init-state {:comm comm}})
              [:button {:onClick (fn [] (om/transact! block :ports #(conj % {})))} "add one"]
              ]])))

(defn handle-event
  [app type value]
  (case type
    :delete-block (om/transact! app :blocks #(vec (remove (partial = value) %)))
    :delete-port (log "TODO")))

(defcomponent app [data owner]
  (init-state [_] {:comm (chan)})
  (will-mount
    [_]
    (let [comm (om/get-state owner :comm)]
      (go (loop []
            (let [[type value] (<! comm)]
              (handle-event data type value)
              (recur))))))
  (render-state [_ state]
    (html [:div
           [:.container
            [:h1 "DESIGNER"]
            [:ul.block-list
             [:h2 "blocks"]
             (om/build-all block-view (:blocks data) {:init-state state})
             [:button {:onClick (fn [] (om/transact! data :blocks #(conj % {})))} "add one"]]]
           [:pre.debug (with-out-str (pprint @data))]])))

(om/root
  app
  app-state
  {:target (. js/document (getElementById "app"))})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
  )

