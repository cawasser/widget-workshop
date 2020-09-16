(ns widget-workshop.views.builder.view
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [widget-workshop.views.dnd.components :as d]
            ["react-beautiful-dnd" :refer [DragDropContext Draggable Droppable]]
            [widget-workshop.views.widget :as w]
            [widget-workshop.handlers.compose-widgets]
            [widget-workshop.util.uuid :refer [aUUID]]
            [widget-workshop.handlers.default-data :refer [gen-widget]]
            [widget-workshop.views.builder.data-table :as t]
            [widget-workshop.views.builder.vega-types :as v]))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; RE-FRAME EVENT HANDLERS
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(rf/reg-event-db
  :new-widget
  (fn [db _]
    (let [new-id (aUUID)]
      ;(prn "new-widget " new-id)
      (assoc db :builder/widget-list (conj (:builder/widget-list db) new-id)
                :widgets (assoc (:widgets db) new-id (gen-widget new-id))))))



(rf/reg-sub
  :widget-content-type
  (fn [db _]
    (get-in db [:widgets (:builder/current-widget db) :content :type])))



(rf/reg-event-db
  :update-widget-content-type
  (fn [db [_ val]]
    (assoc-in db [:widgets (:builder/current-widget db) :content :type] val)))



(rf/reg-sub
  :avail-fields
  (fn [db [_ widget-id]]
    (apply clojure.set/union
      (map #(-> (get-in db [:builder/drag-items % :sample])
              first
              keys
              set)
        (get-in db [:widgets widget-id :source])))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; UI COMPONENTS
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn- sources-drop-area [source widget]
  ;(prn "sources-drop-area" widget source (:sample source) (:columns source) (:row-key-fn source))
  [:div.flow-h
   [:> Droppable {:droppable-id   (str "builder/source-tool-" (:name source))
                  :isDropDisabled false
                  :min-height     "50px"
                  :type           "source"}

    (fn [provided snapshot]
      (r/as-element
        [d/draggable-item-vlist
         provided snapshot
         [source]
         (:id widget)]))]

   (if (not (empty? source))
     [t/data-table (:id widget) (:id source) (atom (:sample source)) (:columns source) (:row-key-fn source)]
     [:div])])



(defn- sources-tool [widget]
  ;(prn "sources-tool" widget)
  [:div
   (map (fn [w]
          ^{:key w} [sources-drop-area @(rf/subscribe [:drag-item w]) widget])
     (:source widget))
   ^{:key "default"} [sources-drop-area "" widget]])



(defn- steps-tool [widget]
  [:div
   [:> Droppable {:droppable-id   "builder/steps-tool"
                  :isDropDisabled false
                  :type           "steps"}

    (fn [provided snapshot]
      ;(prn "steps-tool" (:steps widget))
      (r/as-element
        [d/draggable-item-hlist provided snapshot
         (map (fn [w]
                @(rf/subscribe [:drag-item w]))
           (:steps widget)) (:id widget)]))]])



(defn builder-panel []
  ;(prn ":steps-panel " content)
  (let [current-widget @(rf/subscribe [:buildable-widget])
        widget         @(rf/subscribe [:widget current-widget])]

    [:div
     [:div.panel-block {:style {:margin-bottom           "5px"
                                :border-top-right-radius "5px"
                                :border-top-left-radius  "5px"
                                :border                  :solid
                                :border-width            "1px"
                                :background-color        "lightblue"}}
      [:div
       [:h2.title {:style {:text-align :center}} "Source"]
       [sources-tool widget]]]

     [:div.panel-block {:style {:margin-bottom              "5px"
                                :border-bottom-right-radius "5px"
                                :border-bottom-left-radius  "5px"
                                :border                     :solid
                                :border-width               "1px"
                                :background-color           "lightblue"}}
      [:div
       [:h2.title {:style {:text-align :center}} "Steps"]
       [steps-tool widget]]]]))



(defn sources-panel [content]
  [:> Droppable {:droppable-id   "builder/sources-list"
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
       [:h2.title {:style {:text-align :center}} "Data Sources"]
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
  [:> Droppable {:droppable-id   "builder/steps-list"
                 :isDropDisabled true                       ; can't drop anything onto the source list
                 :type           "steps"}

   (fn [provided snapshot]
     (r/as-element
       [d/draggable-item-vlist provided snapshot content "cadetblue"]))])



(defn steps-sidebar []
  [:div {:style {:border-radius    "5px"
                 :margin-right     "5px"
                 :background-color "lightgray"}}
   [:h2.title {:style {:text-align :center}} "Steps"]
   [steps-panel @(rf/subscribe [:drag-items :builder/steps-list])]])



(defn- build-content [widget]
  (let [type  @(rf/subscribe [:widget-content-type widget])
        ui-base (get v/vega-types-config type)
        as-content (merge ui-base (:content widget))]
    (assoc-in as-content [:base :mark] type)))



(defn widget-ui [widget]
  ;(prn "widget-ui" widget)
  [:div {:style {:border :solid :border-width "1px" :border-radius "5px"
                 :width  "30%" :margin "0px auto 0px auto"}}
   (let [content (build-content widget)
         fields-avail @(rf/subscribe [:avail-fields (:id widget)])]
     (prn "widget-ui content" (-> content :ui :encoding))
     (for [[idx [name field]] (map-indexed vector (get-in content [:ui :encoding]))]
       (do
         (prn "widget-ui field" name field)
         ^{:key idx} [:div.flow-h
                      [:p {:style {:width "20%"}} (:title field)]
                      [:select {:name name :id name}
                       (for [column fields-avail]
                         ^{:key (str column)}
                         [:option {:value column} (str column)])]])))])




(defn vega-input-field [name]
  (fn []
    (let [value @(rf/subscribe [:widget-content-type])]
      ;(prn "vega-input-field-field" name value)
      [:div.button {:class (if (= name value) "are-small is-success" "are-small is-info")
                    :cursor :arrow
                    :style {:margin "1px"}
                    :on-click #(do
                                 (rf/dispatch-sync
                                   [:update-widget-content-type name]))}
       name])))



(defn building-widget-panel []
  [:div {:style {:height "auto"
                 :border :solid :border-width "1px" :border-radius "5px"}}
   (for [[index name] (map-indexed vector v/vega-types)]
     ^{:key index} [vega-input-field name])
   [:hr {:style {:width "75%" :color "black"}}]
   [:div.flow-h
    [w/fullsize-widget @(rf/subscribe [:current-widget])]
    [widget-ui @(rf/subscribe [:current-widget])]]])



(defn- gallery []
  [:div {:style {:border-radius    "5px"
                 :margin           "5px"
                 :padding          "5px"
                 :background-color "lightblue"}}
   [:h2.title "Gallery"]
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
   [:section.section;>div.container;>div.content
    [gallery]
    [:div.columns
     [:div.column.is-one-fifth
      [sources-sidebar]
      [steps-sidebar]]

     [:div.column {:style {:background-color "lightgray"
                           :border-radius    "5px"}}
      [builder-panel]
      [building-widget-panel]]]]])





;;;;;;;;;;;;;;;;;;;;;;;;;
; builder-panel
(comment

  (def db @re-frame.db/app-db)
  (def current-widget @(rf/subscribe [:current-widget]))
  (def widget @(rf/subscribe [:widget current-widget]))
  (def source (:source widget))
  (def steps (:steps widget))


  @(rf/subscribe [:drag-item (:source widget)])

  (def id (:id @(rf/subscribe [:drag-items (:source widget)])))
  (map :id [@(rf/subscribe [:drag-items (:source widget)])])


  (get db :builder/drag-items)
  (-> db
    :builder/drag-items
    (get source))
  (get-in db [:builder/drag-items source])


  (map #(get-in db [:builder/drag-items %]) (:steps widget))

  @(rf/subscribe [:drag-items (:steps widget)])

  (sources-tool @(rf/subscribe [:drag-item (:source widget)]))


  ())





;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; getting the :steps
(comment
  (def db @re-frame.db/app-db)
  (def current-widget @(rf/subscribe [:buildable-widget]))
  (def widget @(rf/subscribe [:widget current-widget]))

  (def steps (:steps widget))
  (def drag-items @(rf/subscribe [:all-drag-items]))

  @(rf/subscribe [:filter-drag-items (:id widget)])

  (let [ret (map #(get drag-items %) steps)]
    ;(prn "found :steps " :steps "//" drag-items "//" ret)
    ret)

  ())


;;;;;;;;;;;;;;;
; quick test of adding a new 'blank' widget
(comment
  (def db @re-frame.db/app-db)
  (rf/dispatch [:new-widget])

  (def widget (get-in db [:widgets "alpha"]))

  (get-in db [:builder/all-drag-items (:source widget)])

  @(rf/subscribe [:drag-items (:source widget)])

  ())



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; get the fields from all the sources in a widget
(comment
  (def db @re-frame.db/app-db)
  (def widget-id "alpha")
  (def source-id (first (get-in db [:widgets widget-id :source])))

  (keys (first (get-in db [:builder/drag-items source-id :sample])))

  (apply clojure.set/union
    (map #(-> (get-in db [:builder/drag-items % :sample])
            first
            keys
            set)
      (get-in db [:widgets widget-id :source])))


  ())

