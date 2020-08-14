(ns widget-workshop.views.home-page
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [widget-workshop.views.dnd.components :as d]
            [widget-workshop.views.dnd.new-widget :refer [new-widget-id]]
            ["react-beautiful-dnd" :refer [DragDropContext Draggable Droppable]]
            [widget-workshop.handlers.dynamic-subscriptions]))





(defn sources-panel [content]
  [:> Droppable {:droppable-id   "data-sources-list"
                 :isDropDisabled true                       ; can't drop anything onto the source list
                 :type           "droppable"}
   (fn [provided snapshot]
     (r/as-element
       [d/draggable-item-vlist provided snapshot content]))])



(defn sources-sidebar []
  (let [the-filter (r/atom "")]
    (fn []
      [:div
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
        @(rf/subscribe [:drag-items :data-sources-list])]])))


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
                  :height       "200px"}}
    [:> Droppable {:droppable-id id :type "droppable"
                   :direction    "horizontal"}
     (fn [provided snapshot]
       (r/as-element
         [d/draggable-item-hlist provided snapshot @(rf/subscribe [:filter-drag-items id])]))]]])


(defn widget-panel []
  [:div {:style {:height "auto"}}
   [:h2 "Widgets"]
   (for [[idx id] (map-indexed vector @(rf/subscribe [:widgets]))]
     ^{:key idx} [widget id true])
   [widget new-widget-id false]])




(defn home-page []
  [:> DragDropContext
   {:onDragStart  #()
    :onDragUpdate #()                                       ;d/on-drag-update (js->clj % :keywordize-keys true)
    :onDragEnd    #(d/on-drag-end (js->clj % :keywordize-keys true))}
   [:section.section>div.container>div.content
    [:div.columns
     [:div.column.is-one-fifth
      {:style {:background-color "lightblue"
               :border-radius    "5px"
               :margin-right     "5px"}}
      [sources-sidebar]]
     [:div.column
      {:style {:background-color "lightgray"
               :border-radius    "5px"}}
      [widget-panel]]]]])



;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; working with re-frame
;
(comment
  @(rf/subscribe [:data-sources])

  (rf/dispatch [:add-source :some-other-source])
  (rf/dispatch [:remove-source :some-other-source])

  (js/console.dir DragDropContext)
  (js/console.dir Draggable)
  (js/console.dir Droppable)



  (into #{}
    ;(map keyword
    (fuzzy-filter "" (map name @(rf/subscribe [:data-sources]))))

  (into #{}
    ;(map keyword
    (fuzzy-filter "me"
      (map name @(rf/subscribe [:data-sources]))))

  (if-let [filter-text (some-> filter-text not-empty .toLowerCase)]
    (filter #(-> %
               .toLowerCase
               (.indexOf filter-text)
               (not= -1))
      alist)
    alist)


  @(rf/subscribe [:filters "alpha"])

  ())



;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; TODO: RICH-COMMENT fuzzy search
;
(comment
  (def f (atom "b"))
  (fuzzy-filter @f (map name @(rf/subscribe [:data-sources])))

  ())



;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; pulling from across multiple keys in app-db
;
(comment
  @re-frame.db/app-db


  @(rf/subscribe [:filter-drag-items id])

  (map (comp name :id) @(rf/subscribe [:drag-items :data-sources-list]))


  (for [[idx {:keys [id name] :as orig}]
        (map-indexed vector
          @(rf/subscribe [:drag-items :data-sources-list]))]
    [id name])

  (for [[idx {:keys [id name] :as orig}]]
    (map-indexed vector
      @(rf/subscribe [:filter-drag-items
                      "e4703951-39e9-4064-ab3c-4e83d1c3787b"]))
    [id name])

  ())