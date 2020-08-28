(ns widget-workshop.nav-bar
  (:require [reagent.core :as r]
            [widget-workshop.session :refer [session]]))


(defn nav-link [uri title page]
  [:a.navbar-item
   {:href   uri
    :active (when (= page (:page @session)) "is-active")}
   title])

(defn navbar []
  (r/with-let [expanded? (r/atom false)]
    [:nav.navbar.is-info>div.container
     [:div.navbar-brand
      [:a.navbar-item {:href "/" :style {:font-weight :bold}} "Widget Workshop"]
      [:span.navbar-burger.burger
       {:data-target :nav-menu
        :on-click    #(swap! expanded? not)
        :class       (when @expanded? :is-active)}
       [:span] [:span] [:span]]]
     [:div#nav-menu.navbar-menu
      {:class (when @expanded? :is-active)}
      [:div.navbar-end
       [nav-link "#/" "Dashboard" :home]
       [nav-link "#/builder" "Builder" :builder]
       [nav-link "#/about" "About" :about]]]]))

