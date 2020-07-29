(ns widget-workshop.views.about-page
  (:require [re-frame.core :as rf]))


(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]
   [:div
    [:p (str ":data-sources " @(rf/subscribe [:data-sources]))]
    [:p (str ":subscriptions " @(rf/subscribe [:subscriptions]))]
    [:p (str ":data " @(rf/subscribe [:data]))]
    [:p (str ":widgets " @(rf/subscribe [:widgets]))]
    [:p (str ":widget-layout " @(rf/subscribe [:widget-layout]))]]])



