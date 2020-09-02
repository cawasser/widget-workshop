(ns widget-workshop.views.builder.view
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [widget-workshop.views.dnd.components :as d]
            ["react-beautiful-dnd" :refer [DragDropContext Draggable Droppable]]
            [widget-workshop.views.widget :as w]
            [widget-workshop.handlers.dynamic-subscriptions]
            [widget-workshop.util.uuid :refer [aUUID]]
            [widget-workshop.handlers.default-data :refer [gen-widget]]))




(rf/reg-event-db
  :new-widget
  (fn [db _]
    (let [new-id (aUUID)]
      ;(prn "new-widget " new-id)
      (assoc db :builder/widget-list (conj (:builder/widget-list db) new-id)
                :widgets (assoc (:widgets db) new-id (gen-widget new-id))))))


(defn- sources-tool [widget]
  [:> Droppable {:droppable-id   "builder/source-list"
                 :isDropDisabled false
                 :min-height     "50px"
                 :type           "source"}

   (fn [provided snapshot]
     (r/as-element
       [d/draggable-item-vlist provided snapshot
        @(rf/subscribe [:source-drag-items (:source widget)]) "cadetblue"]))])



(defn- steps-tool [widget]
  [:> Droppable {:droppable-id   "builder/steps-list"
                 :isDropDisabled false
                 :type           "filter"}

   (fn [provided snapshot]
     (r/as-element
       [d/draggable-item-vlist provided snapshot @(rf/subscribe [:filter-drag-items (:id widget)]) "cadetblue"]))])



(defn builder-panel []
  ;(prn ":steps-panel " content)
  (let [current-widget @(rf/subscribe [:buildable-widget])
        widget         @(rf/subscribe [:widget current-widget])]

    [:div
     [:div.panel-block {:style {:margin-bottom           "5px"
                                :border-top-right-radius "5px"
                                :border-top-left-radius  "5px"
                                :background-color        "lightblue"}}
      [:div
       [:h2 {:style {:text-align :center}} "Source"]
       [sources-tool widget]]]

     [:div.panel-block {:style {:margin-bottom              "5px"
                                :border-bottom-right-radius "5px"
                                :border-bottom-left-radius  "5px"
                                :background-color           "lightgray"}}
      [:div
       [:h2 {:style {:text-align :center}} "Steps:"]
       [steps-tool widget]]]]))



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
        @(rf/subscribe [:drag-items :builder/sources-list])]])))



(defn steps-panel [content]
  ;(prn ":steps-panel " content)
  [:> Droppable {:droppable-id   "builder/filter-list"
                 :isDropDisabled true                       ; can't drop anything onto the source list
                 :type           "filter"}

   (fn [provided snapshot]
     (r/as-element
       [d/draggable-item-vlist provided snapshot content "cadetblue"]))])



(defn steps-sidebar []
  [:div {:style {:border-radius    "5px"
                 :margin-right     "5px"
                 :background-color "lightgray"}}
   [:h2 {:style {:text-align :center}} "Filters"]
   [steps-panel @(rf/subscribe [:drag-items :builder/steps-list])]])



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
    [:button.button.is-large {:style    {:position :relative}
                              :on-click #(rf/dispatch [:new-widget])} "Add"]
    (doall
      (for [w @(rf/subscribe [:buildable-widgets])]
        ^{:key w} [w/small-widget @(rf/subscribe [:widget w])]))]])




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
      [steps-sidebar]]
     [:div.column.is-one-fifth
      [builder-panel]]
     [:div.column {:style {:background-color "lightgray"
                           :border-radius    "5px"}}
      [building-widget-panel]]]]])




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



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; getting the :steps
(comment
  @re-frame.db/app-db

  (def current-widget @(rf/subscribe [:buildable-widget]))
  (def widget @(rf/subscribe [:widget current-widget]))

  (def filters @(rf/subscribe [:steps (:id widget)]))
  (def drag-items @(rf/subscribe [:all-drag-items]))

  @(rf/subscribe [:filter-drag-items (:id widget)])

  (let [ret (map #(get drag-items %) filters)]
    ;(prn "found :steps " :steps "//" drag-items "//" ret)
    ret)

  ())


;;;;;;;;;;;;;;;
; quick test of adding a new 'blank' widget
(comment
  (rf/dispatch [:new-widget])
  ())