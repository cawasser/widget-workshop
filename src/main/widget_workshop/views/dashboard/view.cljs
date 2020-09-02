(ns widget-workshop.views.dashboard.view
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [widget-workshop.views.dnd.components :as d]
            ["react-beautiful-dnd" :refer [DragDropContext Draggable Droppable]]
            [widget-workshop.views.widget :as w]
            [widget-workshop.handlers.compose-widgets]))



(defn- widget-button [item on?]
  [:p.is-6
   {:style    {:border           "1px solid lightgray"
               :border-radius    "5px"
               :padding          "8px"
               :padding-left     "3px"
               :padding-right    "3px"
               :margin-bottom    "8px"
               :max-width        "220px"
               :color            "black"
               :background-color (if @on? "lightgreen" "darkgray")}
    :on-click #(do
                 (swap! on? not)
                 (rf/dispatch-sync [:add-remove-widget (:id item)]))}
   (:name item)])



(defn widgets-panel [content]
  ;(prn "widgets-panel" content)
  (into [:div]
     (for [[index id] (map-indexed vector content)]
       (let [on? (r/atom false)]
        ^{:key index} [widget-button @(rf/subscribe [:widget id]) on?]))))



(defn widgets-sidebar []
  (let [the-filter (r/atom "")]
    (fn []
      [:div {:style {:border-radius    "5px"
                     :margin-right     "5px"
                     :background-color "lightblue"}}
       [:h2 "Toolbox"]
       [:p {:hidden true} @the-filter]                      ; hack to get the droppable to re-render
       [:div.panel-block {:style {:margin-bottom "5px"}}
        [:p.control.has-icons-left
         [:input.input {:type        "text"
                        :placeholder "Search"
                        :on-change   #(reset! the-filter (-> % .-target .-value))}]
         [:span.icon.is-left
          [:i.fas.fa-search {:aria-hidden "true"}]]]]
       [widgets-panel
        ;(fuzzy-filter @the-filter
        ;  (map (comp name :id)
        @(rf/subscribe [:widget-list])]])))


(defn widget-panel []
  [:div
   [:h2 "Widgets"]
   [w/resizable-widget {}]
   [:div.widget-panel]])
    ;(for [[idx id] (map-indexed vector @(rf/subscribe [:widgets]))]
    ;  ^{:key idx} [w/widget id true]))




(defn page []
  [:section.section>div.container>div.content
   [:div.columns
    [:div.column.is-one-fifth
     [widgets-sidebar]]
    [:div.column
     {:style {:background-color "lightgray"
              :border-radius    "5px"}}
     [widget-panel]]]])



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


  @(rf/subscribe [:steps "alpha"])

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


;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; getting the sidebar working
;
(comment
  (def content ["one" "two" "three"])
  (map-indexed vector content)

  (for [[index name] (map-indexed vector content)]
    ;(let [on? (r/atom false)]
    ^{:key index} [:p.is-6 name])

  (def id "one")
  @(rf/subscribe [:widget id])

  ())