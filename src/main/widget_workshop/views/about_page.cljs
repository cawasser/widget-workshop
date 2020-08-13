(ns widget-workshop.views.about-page
  (:require [re-frame.core :as rf]))


(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]
   [:div
    (for [[k v] @re-frame.db/app-db]
      ^{:key k} [:p [:span {:style {:font-weight :bold }}(str k)]
                 [:span " "]
                 [:span (str v)]])]])



