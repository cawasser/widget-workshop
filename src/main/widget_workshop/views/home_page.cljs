(ns widget-workshop.views.home-page
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [cljs.core.match :refer-macros [match]]
            ["react-beautiful-dnd" :refer [DragDropContext Draggable Droppable]]))


; Drag & Drop code mimics:
;
; https://github.com/atlassian/react-beautiful-dnd/issues/427#issuecomment-420563943
;
;
; also drawing inspiration from:
;
;           https://egghead.io/lessons/react-course-introduction-beautiful-and-accessible-drag-and-drop-with-react-beautiful-dnd
;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; dnd handlers
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn on-drag-end [{:keys [draggableId type source destination reason] :as event}]
  (prn "on-drag-end " (keyword (:droppableId source)) (keyword (:droppableId destination)))
  (if destination
    (if (and (= (:droppableId source) (:droppableId destination))
          (= (:index destination) (:index source)))
      (prn "nothing to do")
      (rf/dispatch [:arrange-list
                    (keyword (:droppableId source)) (:index source)
                    (keyword (:droppableId destination)) (:index destination)]))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; data filters
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn fuzzy-filter [filter-text alist]
  (if-let [f (some-> filter-text not-empty .toLowerCase)]
    (into #{}
      (map keyword
        (filter #(-> %
                   .toLowerCase
                   (.indexOf f)
                   (not= -1))
          alist)))
    alist))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; dnd UI components
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn drag-item [id index]
  [:> Draggable {:key id :draggable-id id :index index}
   (fn [provided snapshot]
     (r/as-element
       [:div (merge {:ref (.-innerRef provided)}
               (js->clj (.-draggableProps provided))
               (js->clj (.-dragHandleProps provided)))

        [:p {:key   id
             :index index
             :style {:border           "1px solid lightgray"
                     :border-radius    "5px"
                     :padding          "8px"
                     :padding-left     "3px"
                     :padding-right    "3px"
                     :margin-bottom    "8px"
                     :max-width        "220px"
                     :color            "white"
                     :background-color (if (.-isDraggingOver snapshot)
                                         "green"
                                         "mediumblue")}}
         (str (name id))]]))])



(defn draggable-item-vlist [provided snapshot data]
  (let [isDraggingOver (.-isDraggingOver snapshot)]
    [:div (merge {:ref   (.-innerRef provided)
                  :style {:background-color (if isDraggingOver "lightgreen" "inherit")
                          :border-width     (if isDraggingOver "1px" "inherit")
                          :border-style     "solid"
                          :border-radius    "5px"
                          :margin           "1px"}}
            (js->clj (.-droppableProps provided)))
     (for [[index id] (map-indexed vector data)]
       (drag-item id index))
     (.-placeholder provided)]))


(defn draggable-item-hlist [provided snapshot data]
  (let [isDraggingOver (.-isDraggingOver snapshot)]
    [:div (merge {:ref   (.-innerRef provided)
                  :style {:background-color (if isDraggingOver "lightgreen" "inherit")
                          :border-width     (if isDraggingOver "1px" "inherit")
                          :border-style     "solid"
                          :border-radius    "5px"
                          :margin           "1px"
                          :display          :flex
                          :flex-flow        "row wrap"
                          :justify-contents :middle
                          :align-items      :center}}
            (js->clj (.-droppableProps provided)))
     (for [[index id] (map-indexed vector data)]
       (drag-item id index))
     (.-placeholder provided)]))




(defn sources-panel [content]
  [:> Droppable {:droppable-id "data-sources" :type "droppable"}
   (fn [provided snapshot]
     (r/as-element
       [draggable-item-vlist provided snapshot content]))])



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
        (fuzzy-filter @the-filter
          (map name @(rf/subscribe [:data-sources])))]])))



(defn widget-panel [content]
  [:div
   [:h2 "Widgets"]
   [:div {:style {:border       "solid"
                  :border-width "1px"}}
    [:> Droppable {:droppable-id "filters" :type "droppable" :direction "horizontal"}
     (fn [provided snapshot]
       (r/as-element
         [draggable-item-hlist provided snapshot content]))]]])



(defn home-page []
  [:> DragDropContext
   {:onDragStart  #()
    :onDragUpdate #()
    :onDragEnd    #(on-drag-end (js->clj % :keywordize-keys true))}
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
      [widget-panel @(rf/subscribe [:filters])]]]]])




(comment
  @(rf/subscribe [:data-sources])

  (rf/dispatch [:add-source :some-other-source])
  (rf/dispatch [:remove-source :some-other-source])

  (js/console.dir DragDropContext)
  (js/console.dir Draggable)
  (js/console.dir Droppable)



  (into #{}
    (map keyword
      (fuzzy-filter "" (map name @(rf/subscribe [:data-sources])))))

  (into #{}
    (map keyword
      (fuzzy-filter "me"
        (map name @(rf/subscribe [:data-sources])))))

  (if-let [filter-text (some-> filter-text not-empty .toLowerCase)]
    (filter #(-> %
               .toLowerCase
               (.indexOf filter-text)
               (not= -1))
      alist)
    alist)

  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; from https://github.com/atlassian/react-beautiful-dnd/issues/427
;
(comment
  (def drag-drop-context (r/adapt-react-class js/ReactBeautifulDnd.DragDropContext))
  (def droppable (r/adapt-react-class js/ReactBeautifulDnd.Droppable))
  (def draggable (r/adapt-react-class js/ReactBeautifulDnd.Draggable))

  ; Example drag-drop-context (typically wraps your whole app)
  [drag-drop-context
   {:onDragStart  #(...)
    :onDragUpdate #(...)
    :onDragEnd    #(...)}

   [:div "Render one or more droppables somewhere inside"]]

  ; Example droppable (wraps one of your lists)
  ; Note use of r/as-element and js->clj on droppableProps
  [droppable {:droppable-id "droppable-1" :type "thing"}
   (fn [provided snapshot]
     (r/as-element [:div (merge {:ref   (.-innerRef provided)
                                 :class (when (.-isDraggingOver snapshot) :drag-over)}
                           (js->clj (.-droppableProps provided)))
                    [:h2 "My List - render some draggables inside"]
                    (.-placeholder provided)]))]

  ; Example draggable
  [draggable {:draggable-id "draggable-1", :index 0}
   (fn [provided snapshot]
     (r/as-element [:div (merge {:ref (.-innerRef provided)}
                           (js->clj (.-draggableProps provided))
                           (js->clj (.-dragHandleProps provided)))
                    [:p "Drag me"]]))]


  ())


(comment
  (def atm (atom "string"))

  (def the-string @atm)
  the-string

  (reset! atm "nothing")
  @atm

  (prn @atm)
  (reset! atm "different")

  (defn thinigy [new-val]
    (reset! atm new-val)
    (let [answer (str "/api/something/" @atm)]
      (str answer)))

  (thinigy "nothing")
  (thinigy "something")


  answer





  ())

;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; fuzzy search
;
(comment
  (def f (atom "b"))
  (fuzzy-filter @f (map name @(rf/subscribe [:data-sources])))

  ())