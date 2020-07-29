(ns widget-workshop.main
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [widget-workshop.views.home-page :as home]
            [widget-workshop.nav-bar :as navbar]
            [widget-workshop.session :refer [session]]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [ajax.core :refer [GET POST]]
            [reitit.core :as reitit]
            [clojure.string :as string])
  (:import goog.History))




(def pages
  {:home  #'home/home-page
   :about #'home/about-page})

(defn page []
  [(pages (:page @session))])



;; -------------------------
;; Routes

(def router
  (reitit/router
    [["/" :home]
     ["/about" :about]]))

(defn match-route [uri]
  (->> (or (not-empty (string/replace uri #"^.*#" "")) "/")
    (reitit/match-by-path router)
    :data
    :name))
;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (swap! session assoc :page (match-route (.-token event)))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app

(defn ^:dev/after-load mount-components []
  (rd/render [#'navbar/navbar] (.getElementById js/document "nav-bar"))
  (rd/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-components))




