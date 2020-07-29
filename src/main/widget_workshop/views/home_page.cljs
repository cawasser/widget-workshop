(ns widget-workshop.views.home-page
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))




(defn data-source-panel []
  [:nav.panel
   [:p.panel-heading "Data Sources"]
   [:div.panel-block
    [:p.control.has-icons-left
     [:input.input {:type "text" :placeholder "Search"}]
     [:span.icon.is-left
      [:i.fas.fa-search {:aria-hidden "true"}]]]]
   (for [s @(rf/subscribe [:data-sources])]
     [:a.panel-block.is-active s])])


(defn widget-panel []
  [:nav.panel
   [:p.panel-heading "Widgets"]])


(defn home-page []
  [:section.section>div.container>div.content
   [:div.columns
    [:div.column.is-one-fifth
     {:style {:background-color "lightblue"}}
     (data-source-panel)]
    [:div.column
     {:style {:background-color "lightgray"}}
     (widget-panel)]]])




