(ns widget-workshop.views.home-page
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["react-beautiful-dnd" :refer [DragDropContext Draggable Droppable]]))


; Drag & Drop code mimics:
;
; https://github.com/atlassian/react-beautiful-dnd/issues/427#issuecomment-420563943
;
; but, unfortunately, is NOT working!
;
; seem like the *-dnd JS code is "not" really loaded (Components show as "Anonymous") and then
; we get and error on <PrivateDraggable> if we try to wrap [:> Draggable ...] around the data-source components

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


(defn source [id index]
  ;[:> Draggable {:key id :draggableId id :index index}
  ; (fn [provided snapshot]
  ;   (r/as-element
  ;     [:div (merge {:ref (.-innerRef provided)}
  ;             (js->clj (.-draggableProps provided))
  ;             (js->clj (.-dragHandleProps provided)))
  ;
  [:p {:key   id
       :style {:border           "1px solid lightgray"
               :border-radius    "5px"
               :padding          "8px"
               :margin-bottom    "8px"
               :color            "white"
               :background-color "mediumblue"}}
   (str id)])



(defn sources []
  [:div
   [:h2 "Data Sources"]
   (for [[index id] (map-indexed vector @(rf/subscribe [:data-sources]))]
     (source id index))])



;(defn data-source-panel []
;  (let [the-filter (r/atom "")]
;    (fn []
;      [:nav.panel {:style {:background-color "dodgerblue"}}
;       [:p.panel-heading "Data Sources"]
;       [:div.panel-block
;        [:p.control.has-icons-left
;         [:input.input {:type        "text"
;                        :placeholder "Search"
;                        :on-change   #(reset! the-filter (-> % .-target .-value))}]
;         [:span.icon.is-left
;          [:i.fas.fa-search {:aria-hidden "true"}]]]]
;       (for [[idx s] (map-indexed vector
;                       (fuzzy-filter @the-filter
;                         (map name @(rf/subscribe [:data-sources]))))]
;
;         [:> Draggable {:draggable-id (str idx) :index idx}
;          (fn [provided snapshot]
;            (r/as-element [:div (merge {:ref (.-innerRef provided)}
;                                  (js->clj (.-draggableProps provided))
;                                  (js->clj (.-dragHandleProps provided)))
;                           [:a.panel-block.is-active s]]))])])))



(defn widget-panel []
  [:> Droppable {:droppable-id "droppable-1" :type "thing"}
   (fn [provided snapshot]
     (r/as-element [:div (merge {:ref   (.-innerRef provided)
                                 :class (when (.-isDraggingOver snapshot) :drag-over)}
                           (js->clj (.-droppableProps provided)))
                    [:h2 "Widgets"]
                    [:div {:style {:height "1000px"}}]
                    (.-placeholder provided)]))])



(defn home-page []
  [:> DragDropContext
   {:onDragStart  #()
    :onDragUpdate #()
    :onDragEnd    #()}
   [:section.section>div.container>div.content
    [:div.columns

     [:div.column.is-one-fifth
      {:style {:background-color "lightblue"
               :border-radius    "5px"
               :margin-right     "5px"}}
      [sources]]

     [:div.column
      {:style {:background-color "lightgray"
               :border-radius    "5px"}}
      [widget-panel]]]]])




(comment
  @(rf/subscribe [:data-sources])

  (rf/dispatch [:add-source :some-other-source])
  (rf/dispatch [:remove-source :some-other-source])


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
