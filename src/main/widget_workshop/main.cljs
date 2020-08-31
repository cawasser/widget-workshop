(ns widget-workshop.main
  (:require [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [ajax.core :refer [GET POST]]
            [reitit.core :as reitit]
            [clojure.string :as string]
            [reagent.core :as r]
            [reagent.dom :as rd]
            [re-frame.core :as rf]
            [widget-workshop.views.dashboard.view :as dashboard]
            [widget-workshop.views.builder.view :as builder]
            [widget-workshop.views.about.view :as about]
            [widget-workshop.nav-bar :as navbar]
            [widget-workshop.session :refer [session]]
            [widget-workshop.handlers.initialization]
            [widget-workshop.server.subscriptions :as sm])

  (:import goog.History))




(def pages
  {:home  #'dashboard/page
   :builder #'builder/page
   :about #'about/page})

(defn page []
  [(pages (:page @session))])



;; -------------------------
;; Routes

(def router
  (reitit/router
    [["/" :home]
     ["/builder" :builder]
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
  (rf/dispatch-sync [:initialize])

  ; register some 'data-sources'
  ;(sm/register-data-source "generic-source"
  ;  widget-workshop.server.source.generic-data/get-data)
  ;(sm/register-data-source "config-source"
  ;  widget-workshop.server.source.config-data/get-data)

  ;(sm/register-filter "take" [:take {:param :number}])
  ;(sm/register-filter "extract datetime" [:select [:id :datetime]])

  (mount-components))


(comment
  @re-frame.db/app-db

  ())

