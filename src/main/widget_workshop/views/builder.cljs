(ns widget-workshop.views.builder
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [widget-workshop.views.dnd.components :as d]
            [widget-workshop.views.dnd.new-widget :refer [new-widget-id]]
            ["react-beautiful-dnd" :refer [DragDropContext Draggable Droppable]]
            [widget-workshop.views.widget :as w]
            [widget-workshop.handlers.dynamic-subscriptions]))


(defn sources-panel [content]
  [:> Droppable {:droppable-id   "builder/data-sources-list"
                 :isDropDisabled true                       ; can't drop anything onto the source list
                 :type           "source"}
   (fn [provided snapshot]
     (r/as-element
       [d/draggable-item-vlist provided snapshot content]))])



(defn sources-sidebar []
  (let [the-filter (r/atom "")]
    (fn []
      [:div {:style {:border-radius    "5px"
                     :margin-right     "5px"
                     :background-color "lightblue"}}
       [:h2 "Data Sources"]
       [:p {:hidden true} @the-filter]                      ; hack to get the droppable to re-render
       [:div.panel-block {:style {:margin-bottom "5px"}}
        [:p.control.has-icons-left
         [:input.input {:type        "text"
                        :placeholder "Search"
                        :on-change   #(reset! the-filter (-> % .-target .-value))}]
         [:span.icon.is-left
          [:i.fas.fa-search {:aria-hidden "true"}]]]]
       [sources-panel
        ;(fuzzy-filter @the-filter
        ;  (map (comp name :id)
        @(rf/subscribe [:drag-items :builder/data-sources-list])]])))



(defn filters-panel [content]
  ;(prn "filters-panel " content)
  [:> Droppable {:droppable-id   "builder/filter-list"
                 :isDropDisabled true                       ; can't drop anything onto the source list
                 :type           "filter"}
   (fn [provided snapshot]
     (r/as-element
       [d/draggable-item-vlist provided snapshot content "cadetblue"]))])



(defn filters-sidebar []
  [:div {:style {:border-radius    "5px"
                 :margin-right     "5px"
                 :background-color "lightgray"}}
   [:h2 "Filters"]
   [filters-panel @(rf/subscribe [:drag-items :builder/filter-list])]])




(defn building-widget-panel []
  [:div {:style {:height "auto"}}
   [:h2 "Widgets"]
   (for [[idx id] (map-indexed vector @(rf/subscribe [:buildable-widgets]))]
     ^{:key idx} [w/buildable-widget id true])
   [w/buildable-widget new-widget-id false]])



(defn builder-page []
  [:> DragDropContext
   {:onDragStart  #()
    :onDragUpdate #()                                       ;d/on-drag-update (js->clj % :keywordize-keys true)
    :onDragEnd    #(d/on-drag-end (js->clj % :keywordize-keys true))}
   [:section.section>div.container>div.content
    [:div.columns
     [:div.column.is-one-fifth
      [sources-sidebar]
      [filters-sidebar]]
     [:div.column
      {:style {:background-color "lightgray"
               :border-radius    "5px"}}
      [building-widget-panel]]]]])