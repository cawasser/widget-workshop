(ns widget-workshop.handlers.initialization
  (:require
    [re-frame.core :as rf]
    [cljs-uuid-utils.core :as uuid]))



(rf/reg-event-db
  :initialize

  (fn [db _]
    (assoc db
      :data-sources {}
      :data-sources-list []
      :subscriptions #{}
      :filters {}
      :data {}
      :blank-widget (uuid/uuid-string (uuid/make-random-uuid))
      :widgets []
      :widget-layout {})))



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
    (keys (:data-sources db))))

(rf/reg-sub
  :data-sources-list
  (fn [db _]
    (:data-sources-list db)))


(rf/reg-sub
  :subscriptions
  (fn [db _]
    (:subscriptions db)))

(rf/reg-sub
  :filters
  (fn [db [_ id]]
    (get-in db [:filters id])))

(rf/reg-sub
  :data
  (fn [db _]
    (:data db)))


(rf/reg-sub
  :widgets
  (fn [db _]
    (:widgets db)))

(rf/reg-sub
  :blank-widget
  (fn [db _]
    (:blank-widget db)))

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

  (rf/dispatch-sync [:initialize])

  (rf/dispatch [:subscribe :basic-data])
  (rf/dispatch [:unsubscribe :basic-data])

  (rf/dispatch [:subscribe :bad-data])
  (rf/dispatch [:unsubscribe :bad-data])



  (rf/dispatch [:arrange-list :data-sources 0 2])
  (rf/dispatch [:arrange-list :data-sources 1 2])
  (rf/dispatch [:arrange-list :data-sources 2 0])


  (rf/dispatch [:data-update :basic-data {:name "basic-data"
                                          :data  [1 2 3 4]}])
  (rf/dispatch [:data-update :intermediate-data {:name "intermediate-data"
                                                 :data  {:item-1 [1 1 1 1]
                                                         :item-2 [2 2 2 2]}}])

  ())


; re-arranging the data-sources collection
(comment
  (defonce db {:data-sources [:basic-data :intermediate-data :three]})
  (def ddb (:data-sources db))

  (:data-sources db)

  (def from-idx 1)
  (def to-idx 0)


  (def newColl [1 2 3 4 5 6])

  (defn splice [coll at d & n]
    (let [[a b] (split-at at coll)
          c (drop d b)
          x (if n (concat a n c) (concat a c))]
      (into [] x)))

  (splice r 3 0 1)




  (let [a (splice newColl 0 1)
        b (splice a 2 0 1)]
    b)








  (rf/dispatch [:arrange-list :data-sources 0 1])
  (rf/dispatch [:arrange-list :data-sources 1 2])
  (rf/dispatch [:arrange-list :data-sources 2 0])


  @re-frame.db/app-db



  ())