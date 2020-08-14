(ns widget-workshop.views.dnd.components
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["react-beautiful-dnd" :refer [DragDropContext Draggable Droppable]]
            [widget-workshop.handlers.dynamic-subscriptions]
            [widget-workshop.handlers.scenarios :as s]))


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

(defn on-drag-end
  "handle the 'drag-end' events

  - draggableId: what item is being dragged?
  - type: UNUSED
  - source: (map) showing where (source and index within) the draggable 'came from'
  - destination: (map) showing where (destination and index within) the draggable is 'going to'
  - reason: UNUSED
  - event: the entire (raw) event"

  [{:keys [draggableId type source destination reason] :as event}]

  ;(prn "on-drag-end " event (keyword (:droppableId source)) (keyword (:droppableId destination)))

  (if destination
    (if (and (= (:droppableId source) (:droppableId destination))
          (= (:index destination) (:index source)))
      ()                                                    ;prn "nothing to do"
      (rf/dispatch-sync [:handle-drop-event
                         (:droppableId source) (:index source)
                         (:droppableId destination) (:index destination)]))))






;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; data filters
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn fuzzy-filter [filter-text alist]
  (if-let [f (some-> filter-text not-empty .toLowerCase)]
    (into #{}
      ;(map keyword
      (filter #(-> %
                 .toLowerCase
                 (.indexOf f)
                 (not= -1))
        alist))
    alist))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; dnd UI components
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn drag-item
  ([id name index]
   (drag-item id name index "black" "white"))

  ([id name index bg-color txt-color]
   [:> Draggable {:key id :draggable-id id :index index}
    (fn [provided snapshot]
      (r/as-element
        [:div (merge {:ref (.-innerRef provided)}
                (js->clj (.-draggableProps provided))
                (js->clj (.-dragHandleProps provided)))

         [:p.is-6 {:key   id
                   :index index
                   :style {:border           "1px solid lightgray"
                           :border-radius    "5px"
                           :padding          "8px"
                           :padding-left     "3px"
                           :padding-right    "3px"
                           :margin-bottom    "8px"
                           :max-width        "220px"
                           :color            txt-color
                           :background-color bg-color}}
          name]]))]))




(defn draggable-item-vlist [provided snapshot data]
  (let [isDraggingOver (.-isDraggingOver snapshot)]
    [:div (merge {:ref   (.-innerRef provided)
                  :style {:background-color (if isDraggingOver "lightgreen" "inherit")
                          :border-width     (if isDraggingOver "1px" "inherit")
                          :border-style     "solid"
                          :border-radius    "5px"
                          :min-height       "30px"
                          :margin           "1px"}}
            (js->clj (.-droppableProps provided)))
     ;(prn "draggable-item-vlist " data)
     (for [[index {:keys [id name]}] (map-indexed vector data)]
       (drag-item id name index "mediumblue" "white"))
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
                          :min-height       "50px"
                          :flex-flow        "row wrap"
                          :justify-contents :middle
                          :align-items      :center}}
            (js->clj (.-droppableProps provided)))
     ;(prn "draggable-item-hlist " data)
     (for [[index {:keys [id name]}] (map-indexed vector data)]
       (drag-item id name index))
     (.-placeholder provided)]))
