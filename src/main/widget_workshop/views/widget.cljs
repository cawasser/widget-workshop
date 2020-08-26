(ns widget-workshop.views.widget
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["react-beautiful-dnd" :refer [DragDropContext Draggable Droppable]]
            [widget-workshop.views.dnd.components :as d]))



(defn- title-bar [id can-delete]
  [:div.container.level
   [:div.level-left.has-text-left
    [:h3 {:style {:height           "30px"
                  :background-color (if can-delete "tomato" "darkgray")}} id]
    (if can-delete
      [:div.level-right.has-text-centered
       [:button.delete.is-large {:style    {:margin-right "10px"}
                                 :on-click #(do
                                              ;(prn "delete button " id)
                                              (rf/dispatch [:remove-widget id])
                                              (.stopPropagation %))}]])]])


(defn widget [id can-delete]
  ;(prn "widget " id @(rf/subscribe [:filters id]) @(rf/subscribe [:filter-drag-items id]))
  [:div {:style {:border       "solid"
                 :border-width "1px"
                 :height       "auto"
                 :width        "500px"}}
   [title-bar id can-delete]
   [:div {:style {:border       "solid"
                  :border-width "1px"
                  :height       "200px"}}]])


(defn buildable-widget [id can-delete]
  ;(prn "widget " id @(rf/subscribe [:filters id]) @(rf/subscribe [:filter-drag-items id]))
  [:div {:style {:border       "solid"
                 :border-width "1px"
                 :height       "auto"
                 :width        "500px"}}
   [title-bar id can-delete]
   [:div {:style {:border       "solid"
                  :border-width "1px"
                  :height       "200px"
                  :flex-flow        "row wrap"}}

    [:> Droppable {:droppable-id (str id "-source")
                   :type         "source"
                   :direction    "horizontal"}
     (fn [provided snapshot]
       (r/as-element
         [d/draggable-item-hlist provided snapshot @(rf/subscribe [:filter-drag-items (str id "-source")])]))]

    [:> Droppable {:droppable-id (str id "-filters")
                   :type         "filter"
                   :direction    "horizontal"}
     (fn [provided snapshot]
       (r/as-element
         [d/draggable-item-hlist provided snapshot @(rf/subscribe [:filter-drag-items (str id "-filters")])]))]]])
