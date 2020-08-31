(ns widget-workshop.views.builder.view
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [widget-workshop.views.dnd.components :as d]
            [widget-workshop.views.dnd.new-widget :refer [new-widget-id new-widget-context]]
            ["react-beautiful-dnd" :refer [DragDropContext Draggable Droppable]]
            [widget-workshop.views.widget :as w]
            [widget-workshop.handlers.dynamic-subscriptions]))


(defn builder-panel []
  ;(prn "filters-panel " content)
  (let [current-widget @(rf/subscribe [:buildable-widget])
        widget         @(rf/subscribe [:widget current-widget])]

    [:div {:style {:border-radius    "5px"
                   :margin-right     "5px"
                   :background-color "lightblue"}}
     [:h2 {:style {:text-align :center}} "Content"]
     [:div.panel-block {:style {:margin-bottom "5px"}}
      [:div
       [:p.is-5 "source:"]
       [:> Droppable {:droppable-id   "builder/source-list"
                      :isDropDisabled false
                      :type           "source"}

        (fn [provided snapshot]
          (r/as-element
            [d/draggable-item-hlist provided snapshot @(rf/subscribe [:source-drag-items (:source widget)]) "cadetblue"]))]]

      [:div
       [:p.is-5 "steps:"]]]]))
;[:> Droppable {:droppable-id   "builder/filter-list"
;               :isDropDisabled false
;               :type           "filter"}
;
; (fn [provided snapshot]
;   (r/as-element
;     [d/draggable-item-vlist provided snapshot @(rf/subscribe [:filter-drag-items (:id widget)]) "cadetblue"]))]]))






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
       [:h2 {:style {:text-align :center}} "Data Sources"]
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
   [:h2 {:style {:text-align :center}} "Filters"]
   [filters-panel @(rf/subscribe [:drag-items :builder/filter-list])]])



(defn building-widget-panel []
  [:div {:style {:height "auto"}}
   [w/fullsize-widget @(rf/subscribe [:buildable-widgets])]])



(defn- gallery []
  [:div {:style {:border-radius    "5px"
                 :margin           "5px"
                 :padding          "5px"
                 :background-color "lightblue"}}
   [:h2 "Gallery"]
   [:div.gallery {}
    (doall
      (for [w @(rf/subscribe [:buildable-widgets])]
        ^{:key w} [w/small-widget @(rf/subscribe [:widget w])]))
    [:button.button.is-large {:style {:position :relative}} "Add"]]])



(defn page []
  [:> DragDropContext
   {:onDragStart  #()
    :onDragUpdate #()                                       ;d/on-drag-update (js->clj % :keywordize-keys true)
    :onDragEnd    #(d/on-drag-end (js->clj % :keywordize-keys true))}
   [:section.section>div.container>div.content
    [gallery]
    [:div.columns
     [:div.column.is-one-fifth
      [sources-sidebar]
      [filters-sidebar]]
     [:div.column.is-one-fifth
      [builder-panel]]
     [:div.column {:style {:background-color "lightgray"
                           :border-radius    "5px"}}
      [building-widget-panel]]]]])




; TODO: the drop targets can move OUT of the widget and into their own panel
; code for drop targets that needs a new home
(comment

  (defn prep-id [id suffix]
    ;(prn "prep-id" id widget-workshop.views.dnd.new-widget/new-widget-id suffix)
    ;(if (= widget-workshop.views.dnd.new-widget/new-widget-id id)
    ;  widget-workshop.views.dnd.new-widget/new-widget-id
    (str id suffix))

  source-id (prep-id id "@source")
  filter-id (prep-id id "@filter")

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
       [d/draggable-item-hlist provided snapshot @(rf/subscribe [:filter-drag-items id])]))])


;;;;;;;;;;;;;;;;;;;;;;;;;
; builder-panel
;
(comment
  (def current-widget @(rf/subscribe [:buildable-widget]))
  (def widget @(rf/subscribe [:widget current-widget]))

  (w/fullsize-widget @(rf/subscribe [:widget current-widget]))

  (:source widget)


  (def id (-> @(rf/subscribe [:source-drag-items (:source widget)]) first :id))
  (map :id @(rf/subscribe [:source-drag-items (:source widget)]))

  (def source @(rf/subscribe [:source (:source widget)]))
  (def drag-items @(rf/subscribe [:all-drag-items]))

  (if source
    (let [ret (map #(get drag-items %) source)]
      ;(prn "found source " source "//" drag-items "//" ret)
      ret)
    [])


  ())