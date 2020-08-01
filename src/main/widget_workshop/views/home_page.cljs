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
                     :margin-bottom    "8px"
                     :color            "white"
                     :background-color "mediumblue"}}
         (str (name id))]]))])



(defn sources-list [the-filter]
  [:div
   (for [[index id] (map-indexed vector
                      (fuzzy-filter the-filter
                        (map name @(rf/subscribe [:data-sources]))))]
     (source id index))])



(defn sources-panel []
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

       [:> Droppable {:droppable-id "sources-panel" :type "sources-panel"}
        (fn [provided snapshot]
          (r/as-element
            [:div (merge {:ref   (.-innerRef provided)
                          :class (when (.-isDraggingOver snapshot) :drag-over)}
                    (js->clj (.-droppableProps provided)))
             [sources-list @the-filter]
             (.-placeholder provided)]))]])))



(defn widget-panel []
  [:div
   [:h2 "Widgets"]
   [:> Droppable {:droppable-id "widget-panel" :type "widget-panel"}
    (fn [provided snapshot]
      (r/as-element [:div (merge {:ref   (.-innerRef provided)
                                  :class (when (.-isDraggingOver snapshot) :drag-over)}
                            (js->clj (.-droppableProps provided)))
                     [:div {:style {:height "1000px"}}
                      [:p "drop sources-panel here"]]
                     (.-placeholder provided)]))]])


(defn on-drag-end [{:keys [draggableId source destination]}]
  (if (= (:droppableId source) (:droppableId destination))
    (prn "dropped on SELF")
    (prn "dropped on valid target")))



(defn home-page []
  [:> DragDropContext
   {:onDragStart  #()
    :onDragUpdate #()
    :onDragEnd    #(on-drag-end %)}
   [:section.section>div.container>div.content
    [:div.columns

     [:div.column.is-one-fifth
      {:style {:background-color "lightblue"
               :border-radius    "5px"
               :margin-right     "5px"}}
      [sources-panel]]

     [:div.column
      {:style {:background-color "lightgray"
               :border-radius    "5px"}}
      [widget-panel]]]]])




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
