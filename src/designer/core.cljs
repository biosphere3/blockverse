(ns ^:figwheel-always designer.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [cljs.pprint :refer (pprint)]
            [cljs.reader :refer (read-string)]
            [cljs.core.async :refer [put! chan <!]]
            [cljs-uuid-utils.core :as uuid]

            [om.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]

            [designer.util :refer (keyed trace)]
            ))

(enable-console-print!)

(defn log [x] (.log js/console x))

(def rand-uuid (comp str uuid/make-random-uuid))

(defrecord Block [uuid name])
(defrecord Port [type block-uuid name scalar units])

(def initial-state
  (let [blocks [(->Block (rand-uuid) "humanoid")]
        blockmap (keyed :name blocks)
        b #(get-in blockmap [% :uuid])
        ports [(->Port :input (b "humanoid") "food" 360 "pounds per year")
               (->Port :input (b "humanoid") "water" 2 "L per day")]]
    {:blocks blocks :ports ports}))

(def app-state (atom initial-state))

(defn get-block-ports
  [ports block]
  (filter #(= (get % :block-uuid) (:uuid block)) ports))

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

(defcomponent block-view [block owner {:keys [all-ports]}]
  (render-state [this {:keys [comm] :as state}]
          (html
            [:li.block
             [:button {:onClick #(put! comm [:delete-block @block])} "×"]
             [:label "name " [:input {:value (:name block)
                                      :onChange (edit-fn block :name str)}]]
             [:ul.port-list
              (om/build-all port-view (get-block-ports all-ports @block) {:init-state {:comm comm}})
              [:button {:onClick #(put! comm [:add-port [(:uuid @block) owner]])} "add one"]
              ]])))

(defn handle-event
  [data type value]
  (case type
    :delete-block (let [block value] (om/transact! data :blocks #(vec (remove (partial = block) %))))
    :delete-port (log "TODO")
    :add-port (let [[block owner] value]
                (om/transact! data :ports #(vec (conj % {:block-uuid block
                                                         :type :input})))
                (om/refresh! owner))))

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
             (om/build-all block-view (:blocks data) {:init-state state :opts {:all-ports (:ports data)}})
             [:button {:onClick (fn [] (om/transact! data :blocks #(conj % (->Block (rand-uuid) ""))))} "add one"]]]
           [:.debug
            [:h2 "output"]
            [:textarea
             (as-> (select-keys @data [:blocks :ports]) $
                   (clj->js $)
                   (. js/JSON (stringify $ nil 2)))]]])))

(om/root
  app
  app-state
  {:target (. js/document (getElementById "app"))})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
  )

