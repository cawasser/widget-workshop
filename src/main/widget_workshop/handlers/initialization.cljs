(ns widget-workshop.handlers.initialization
  (:require
    [re-frame.core :as rf]))



(rf/reg-event-db
  :initialize

  (fn [db _]
    (assoc db
      :data-sources #{:basic-data :intermediate-data}
      :subscriptions #{}
      :data {}
      :widgets {}
      :widget-layout {})))



(rf/reg-event-db
  :add-source
  (fn [db [_ source-name]]
    (assoc db :data-sources (conj (:data-sources db) source-name))))

(rf/reg-event-db
  :remove-source
  (fn [db [_ source-name]]
    (assoc db :data-sources (disj (:data-sources db) source-name))))


(rf/reg-event-db
  :subscribe
  (fn [db [_ source-name]]
    (prn source-name)
    (if (contains? (:data-sources db) source-name)
      (assoc db :subscriptions (conj (:subscriptions db) source-name))
      db)))


(rf/reg-event-db
  :unsubscribe
  (fn [db [_ source-name]]
    (assoc db :subscriptions (disj (:subscriptions db) source-name))))


(rf/reg-event-db
  :data-update
  (fn [db [_ source-name source-data]]
    (assoc-in db [:data source-name] source-data)))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; subscription to main data elements
;

(rf/reg-sub
  :data-sources
  (fn [db _]
    (:data-sources db)))

(rf/reg-sub
  :subscriptions
  (fn [db _]
    (:subscriptions db)))


(rf/reg-sub
  :data
  (fn [db _]
    (:data db)))


(rf/reg-sub
  :widgets
  (fn [db _]
    (:widgets db)))

(rf/reg-sub
  :widget-layout
  (fn [db _]
    (:widget-layout db)))





;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; let's play with the app-db
;
(comment

  (def app-db (atom @re-frame.db/app-db))

  (rf/dispatch [:subscribe :basic-data])
  (rf/dispatch [:unsubscribe :basic-data])

  (rf/dispatch [:subscribe :bad-data])
  (rf/dispatch [:unsubscribe :bad-data])



  (rf/dispatch [:add-source :really-big-source])
  (rf/dispatch [:remove-source :really-big-source])



  (rf/dispatch [:data-update :basic-data {:name "basic-data"
                                          :data  [1 2 3 4]}])
  (rf/dispatch [:data-update :intermediate-data {:name "intermediate-data"
                                                 :data  {:item-1 [1 1 1 1]
                                                         :item-2 [2 2 2 2]}}])

  ())
