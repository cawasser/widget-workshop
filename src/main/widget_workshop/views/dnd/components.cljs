(ns widget-workshop.views.dnd.components
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [re-frame.core :as rf]
            ["react-beautiful-dnd" :refer [DragDropContext Draggable Droppable]]
            [widget-workshop.handlers.compose-widgets]
            [widget-workshop.handlers.scenarios :as s]
            [widget-workshop.views.dnd.edit-panel :as e]
            ["react-simple-popover" :as Popover]))


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

  ;(prn "on-drag-end " event (:droppableId source) (:droppableId destination))
  ;(.substr (:droppableId destination) 0 (.indexOf (:droppableId destination) "-")))

  (if destination
    ;(let [clean-dest (s/strip-suffix (:droppableId destination))]
    (if (not (and (= (:droppableId source) (:droppableId destination))
               (= (:index destination) (:index source))))
      (rf/dispatch-sync [:handle-drop-event
                         (:droppableId source) (:index source)
                         (:droppableId destination) (:index destination)])
      (do
        (prn "do nothing"
          ())))))



(defn- get-colors [type]
  (if type
    (condp = type
      :source ["cornflowerblue" "black"]
      :step ["cadetblue" "white"]
      ["black" "white"])
    ["black" "white"]))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; data :steps
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




(defn- popover [this ref content isActive?]
  [:> Popover
   {:placement :right
    :container this
    :target    ref
    :show      @isActive?
    :onHide    #(swap! isActive? not)}
   content])


(defn source-drag-item [{:keys [id name type] :as item} index]
  (let [[bg-color txt-color] (get-colors type)
        isActive? (r/atom false)]
    (prn "source-drag-item" id name type bg-color txt-color)

    [:> Draggable {:key id :draggable-id id :index index}
     (fn [provided snapshot]
       (r/as-element
         [:div (merge {:ref (.-innerRef provided)}
                 (js->clj (.-draggableProps provided))
                 (js->clj (.-dragHandleProps provided)))

          ;[Popover (rd/dom-node ) (.-innerRef provided)
          ; (str (:sample item)) isActive?]

          [:div.flow-h {:key   id
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
           [:span name]
           [:span {:style    {:color       txt-color
                              :margin-left "10px"
                              :cursor      "e-resize"}
                   :on-click #(prn "show the meta-data")} ">"]]]))]))



(defn step-drag-item [{:keys [id name type step static] :as item} index]
  (let [[bg-color txt-color] (get-colors type)
        isOpen? (r/atom false)]
    (prn "step-drag-item" item id name type step)

    [:> Draggable {:key id :draggable-id id :index index}
     (fn [provided snapshot]
       (r/as-element
         [:div (merge {:ref (.-innerRef provided)}
                 (js->clj (.-draggableProps provided))
                 (js->clj (.-dragHandleProps provided)))

          [:div.flow-h {:key   id
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
           [:p name]
           (if (not static)
             [:div {:style           {:color       "lightgray"
                                      :margin-left "10px"
                                      :cursor      :default}}
              (e/edit-panel item isOpen?)])
           (if (not static)
             [:div {:style    {:color       txt-color
                               :margin-left "10px"
                               :cursor      "e-resize"}
                    :on-click #(prn "show the step")} ">"])]]))]))



(defn drag-item
  "normally we would do something like this as multi-methods, but Reagent has
  trouble with multimethods for rendering

  see https://stackoverflow.com/questions/33299746/why-are-multi-methods-not-working-as-functions-for-reagent-re-frame"

  [{:keys [id name type] :as item} index]

  (condp = type
    :source (source-drag-item item index)
    :step (step-drag-item item index)
    [:div]))



(defn draggable-item-vlist [provided snapshot data]
  (let [isDraggingOver (.-isDraggingOver snapshot)]
    [:div (merge {:ref   (.-innerRef provided)
                  :style {:background-color (if isDraggingOver "lightgreen" "inherit")
                          :border-width     (if isDraggingOver "1px" "inherit")
                          :border-style     "solid"
                          :border-radius    "5px"
                          :min-height       "30px"
                          :margin           "auto"}}
            (js->clj (.-droppableProps provided)))
     (prn "draggable-item-vlist " data)
     (for [[index item] (map-indexed vector data)]
       ^{:key index} (drag-item item index))
     (.-placeholder provided)]))



(defn draggable-item-hlist [provided snapshot data]
  (let [isDraggingOver (.-isDraggingOver snapshot)]
    [:div (merge {:ref   (.-innerRef provided)
                  :style {:background-color (if isDraggingOver "lightgreen" "inherit")
                          :border-width     (if isDraggingOver "1px" "inherit")
                          :border-style     "solid"
                          :border-radius    "5px"
                          :margin           "auto"
                          :display          :flex
                          :min-height       "50px"
                          :flex-flow        "row wrap"
                          :justify-contents :middle
                          :align-items      :center}}
            (js->clj (.-droppableProps provided)))
     ;(prn "draggable-item-hlist " data)
     (for [[index item] (map-indexed vector data)]
       ^{:key index} (drag-item item index))
     (.-placeholder provided)]))




(comment
  (.substr "testing-source" 0 (.indexOf "testing-source" "-"))


  (empty? [nil])
  ())