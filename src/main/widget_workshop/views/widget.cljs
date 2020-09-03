(ns widget-workshop.views.widget
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["react-beautiful-dnd" :refer [DragDropContext Draggable Droppable]]
            [widget-workshop.views.dnd.components :as d]
            [widget-workshop.views.oz.content :as oz]))




(rf/reg-event-db
  :current-widget
  (fn [db [_ id]]
    (assoc db :builder/current-widget id)))


(rf/reg-sub
  :current-widget-id
  (fn [db _]
    (:builder/current-widget db)))


(rf/reg-sub
  :current-widget
  (fn [db _]
    (get-in db [:widgets (:builder/current-widget db)])))



(defn- title-bar [widget delete?]
  ;(prn "title-bar" widget delete?)
  [:div#title-bar.container.level {:style {:width            "auto"
                                           :height           "auto"
                                           :background-color (:title-color widget)
                                           :color            (:text-color widget)}}
   [:div.level-left.has-text-left
    {:style {:width  "auto"
             :height "auto"}}
    [:h3 {:style {:margin   5
                  :position :relative
                  :top      "50%"}}
     (:name widget)]
    (if delete?
      [:div.level-right.has-text-centered
       {:style {:position     :absolute :top "50%" :right "0%"
                :margin-right "10px" :margin-left "5px"}}
       [:button.delete.is-large {:on-click #(do
                                              ;(prn "delete button " id)
                                              (rf/dispatch [:remove-widget (:id widget)])
                                              (.stopPropagation %))}]])]])


(defn- handle-content [widget]
  [oz.core/vega-lite @oz/line-plot])



(defn small-widget [widget]
  ;(prn "small-widget" widget @(rf/subscribe [:current-widget-id]))
  [:div {:on-click #(rf/dispatch [:current-widget (:id widget)])}
   [:div.widget {:style {:width        "150px" :height "80px"
                         :border-width (if (= (:id widget) @(rf/subscribe [:current-widget-id]))
                                         "5px"
                                         "1px")
                         :border-color (if (= (:id widget) @(rf/subscribe [:current-widget-id]))
                                         "blue"
                                         "gray")}}]
   ;[handle-content widget]]
   [:p.is-6 {:style {:text-align :center}} (:name widget)]])



(defn resizable-widget [widget]
  ;(prn "resizable-widget" widget)
  [:div.widget {:style {:width "500px" :height "300px"}}
   [title-bar widget true]
   [handle-content widget]])



(defn fullsize-widget [widget]
  ;(prn "fullsize-widget" widget)
  [:div.widget
   [title-bar widget false]
   [handle-content widget]])



(comment
  (def id "some-uuid")
  (= widget-workshop.views.dnd.new-widget-id id)

  (if (= widget-workshop.views.dnd.new-widget/new-widget-id id)
    widget-workshop.views.dnd.new-widget/new-widget-id
    (str id suffix))
  ())


