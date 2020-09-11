(ns widget-workshop.views.widget
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["react-beautiful-dnd" :refer [DragDropContext Draggable Droppable]]
            [widget-workshop.views.dnd.components :as d]
            [widget-workshop.views.oz.content :as oz]
            [widget-workshop.views.oz.themes :as themes]
            [widget-workshop.views.builder.sample-pipeline :as p]))




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



(rf/reg-sub
  :widget-source
  (fn [db [_ id]]
    (let [ret (get-in db [:widgets id :source])]
      ;(prn ":widget-source" id ret)
      ret)))

(rf/reg-sub
  :build/sources
  (fn [db _]
    (if-let [ret (:builder/drag-items db)]
      (do
        ;(prn ":build/sources" ret)
        ret)
      (do
        ;(prn ":build/sources []")
        []))))




(rf/reg-sub
  :widget-source-sample
  (fn [[_ id]]
   ;(prn "pre :widget-source-sample" id)
    (if (not (empty? id))
      [(rf/subscribe [:widget-source id]) (rf/subscribe [:build/sources])]
      []))

  (fn [[source-id sources]]
    ;(prn ":widget-source-sample" source-id sources)
    (if (and
          (not (empty? sources))
          (not= "" source-id))
      (get-in sources [source-id :sample])
      [])))





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


(defn- handle-content [widget type]
  [oz.core/vega-lite (-> (merge themes/darkTheme @oz/plot)
                       (assoc :mark type)
                       (assoc-in [:data :values] (oz/play-data "sample")))])



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



(defn- handle-sample-data [widget]
  ;(prn "handle-sample-data" widget (:id widget))
  (let [id (:id widget)
        source (rf/subscribe [:widget-source-sample id])
        pipeline (rf/subscribe [:widget-pipeline id])]

    (fn []
      (let [after (p/run-pipeline @pipeline @source)]
        ;(prn "handle-sample-data ===>" after)
        [oz.core/vega-lite (-> @oz/plot
                             (assoc-in [:data :values] after)
                             (assoc :mark @(rf/subscribe [:vega-type])))]))))



(defn fullsize-widget [widget]
  ;(prn "fullsize-widget" widget)
  [:div.widget
   [title-bar widget false]
   [handle-sample-data widget]])



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment
  (def id "some-uuid")
  (= widget-workshop.views.dnd.new-widget-id id)

  (if (= widget-workshop.views.dnd.new-widget/new-widget-id id)
    widget-workshop.views.dnd.new-widget/new-widget-id
    (str id suffix))
  ())



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; get the sample data for the source of the current-widget
(comment
  (def db @re-frame.db/app-db)
  (def widget @(rf/subscribe [:current-widget]))
  (def id (:id @(rf/subscribe [:current-widget])))

  (get-in db [:widgets id :source])
  @(rf/subscribe [:widget-source id])

  (:builder/drag-items db)
  @(rf/subscribe [:build/sources])

  (if-let [ret (:builder/drag-items db)]
    ret
    [])

  (let [[id sources] [@(rf/subscribe [:widget-source id])
                      @(rf/subscribe [:build/sources])]]
    (if (and
          (not (empty? sources))
          (not= "" id))
      (get-in sources [id :sample])
      []))


  @(rf/subscribe [:widget-source-sample (:source widget)])



  (-> db
    (assoc-in [:widgets current-widget]
      (replace-source db widget new-uuid))
    (assoc-in [:builder/drag-items new-uuid]
      (assoc item :id new-uuid)))


  ())



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; HACK!
;
; format the sample data for the source of the current-widget so
; it breaks lines after each hash-map
(comment
  (def id (:id @(rf/subscribe [:current-widget])))
  (def source (rf/subscribe [:widget-source-sample id]))
  (def pipeline (rf/subscribe [:widget-pipeline id]))

  (def x (p/run-pipeline
           @(rf/subscribe [:widget-source-sample id])
           @(rf/subscribe [:widget-pipeline id])))

  (let [source (rf/subscribe [:widget-source-sample id])
        pipeline (rf/subscribe [:widget-pipeline id])]
    (p/run-pipeline @pipeline @source))

  ())


