(ns widget-workshop.views.home-page
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            ["react-beautiful-dnd" :refer [DragDropContext Droppable Draggable]]))



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


(defn data-source-panel []
  (let [the-filter (r/atom "")]
    (fn []
      [:nav.panel {:style {:background-color "dodgerblue"}}
       [:p.panel-heading "Data Sources"]
       [:div.panel-block
        [:p.control.has-icons-left
         [:input.input {:type        "text"
                        :placeholder "Search"
                        :on-change   #(reset! the-filter (-> % .-target .-value))}]
         [:span.icon.is-left
          [:i.fas.fa-search {:aria-hidden "true"}]]]]
       (for [[idx s] (map-indexed vector
                      (fuzzy-filter @the-filter
                        (map name @(rf/subscribe [:data-sources]))))]
         [:a.panel-block.is-active.Draggable {:droppable-id idx :index idx} s])])))


(defn widget-panel []
  [:nav.panel.Droppable
   [:p.panel-heading "Widgets"]])


(defn home-page []
  [:section.section>div.container>div.content
   [:div.columns.DragDropContext {:on-drag-end #(prn "Dropped!")}
    [:div.column.is-one-fifth
     {:style {:background-color "lightblue"}}
     [data-source-panel]]
    [:div.column
     {:style {:background-color "lightgray"
              :height "1000px"}}
     (widget-panel)]]])




(comment
  @(rf/subscribe [:data-sources])


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

