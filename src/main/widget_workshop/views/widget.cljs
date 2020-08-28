(ns widget-workshop.views.widget
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["react-beautiful-dnd" :refer [DragDropContext Draggable Droppable]]
            [widget-workshop.views.dnd.components :as d]
            [widget-workshop.views.dnd.new-widget :refer [new-widget-id]]))



(defn- title-bar [context can-delete]
  ;(prn "title-bar" context can-delete)
  [:div.container.level
   [:div.level-left.has-text-left
    [:h3 {:style {:height           "30px"
                  :background-color (:title-color context)
                  :color            (:text-color context)}}
     (:name context)]
    (if can-delete
      [:div.level-right.has-text-centered
       [:button.delete.is-large {:style    {:margin-right "10px"}
                                 :on-click #(do
                                              ;(prn "delete button " id)
                                              (rf/dispatch [:remove-widget (:id context)])
                                              (.stopPropagation %))}]])]])


(defn widget [id can-delete]
  ;(prn "widget " id @(rf/subscribe [:filters id]) @(rf/subscribe [:filter-drag-items id]))
  [:div.widget
   [title-bar @(rf/subscribe [:buildable-widget id]) can-delete]
   [:div {:style {:border       "solid"
                  :border-width "1px"
                  :height       "200px"}}]])


(defn prep-id [id suffix]
  ;(prn "prep-id" id widget-workshop.views.dnd.new-widget/new-widget-id suffix)
  ;(if (= widget-workshop.views.dnd.new-widget/new-widget-id id)
  ;  widget-workshop.views.dnd.new-widget/new-widget-id
  (str id suffix))

(defn buildable-widget [id can-delete]
  (let [source-id (prep-id id "@source")
        filter-id (prep-id id "@filter")
        context   @(rf/subscribe [:buildable-widget id])]

    ;(prn "buildable-widget" id
    ;  source-id filter-id
    ;  @(rf/subscribe [:source id])
    ;  @(rf/subscribe [:source-drag-items id])
    ;  @(rf/subscribe [:filters id])
    ;  @(rf/subscribe [:filter-drag-items id])
    ;  context)

    [:div.widget
     [title-bar context can-delete]
     [:div {:style {:border       "solid"
                    :border-width "1px"
                    :height       "200px"
                    :flex-flow    "row wrap"}}

      [:> Droppable {:droppable-id source-id
                     :type         "source"
                     :direction    "horizontal"}
       (fn [provided snapshot]
         (r/as-element
           [d/draggable-item-hlist provided snapshot @(rf/subscribe [:source-drag-items id])]))]

      [:> Droppable {:droppable-id filter-id
                     :type         "filter"
                     :direction    "horizontal"}
       (fn [provided snapshot]
         (r/as-element
           [d/draggable-item-hlist provided snapshot @(rf/subscribe [:filter-drag-items id])]))]]]))


(comment
  (def id "some-uuid")
  (= widget-workshop.views.dnd.new-widget-id id)

  (if (= widget-workshop.views.dnd.new-widget/new-widget-id id)
    widget-workshop.views.dnd.new-widget/new-widget-id
    (str id suffix))
  ())