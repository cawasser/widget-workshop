(ns widget-workshop.handlers.initialization
  (:require
    [re-frame.core :as rf]
    [widget-workshop.util.uuid :refer [aUUID]]))



(rf/reg-event-db
  :initialize

  (fn [db _]
    (assoc db

      :data-sources {}
      ; map of the data-sources, with id mapped to the function
      ;
      ; used to generate the most up-to-date data

      :data-sources-list []
      ; list of :data-sources id's. should always contain the same uuids as
      ; :data-sources
      ;
      ; used to create the UI for dragging,

      :subscriptions #{}
      ;
      ;

      :filters {}
      ; map of UUIDs to uniquely identify a draggable item, each mapped to {:id} which
      ; provides human-readable naming for the item
      ;
      ; used to create each draggable in the UI (sidebar or widgets)

      :data {}
      ;
      ;

      :blank-widget (aUUID)
      ; UUID for a 'blank' widget, show at the bottom of the widgets panel UI
      ;
      ; used to denote the 'special' widget for 'widget creation', this uuid will
      ; be migrated into :data-sources and :data-sources-list when promted to a
      ; 'real' widget upon first drop event

      :widgets []
      ; uuids for 'real' widgets
      ;
      ; used to generate the widgets in the widget panel UI

      :widget-layout {}
      ;
      ;

      :drag-items {})))
      ; map of uuids to {:id <uuid> :name <name>} for each draggble, so they are uniquely identified
      ; throughout the entire app
      ;
      ; use to identify draggable items for use in the UI, as well as any data needed for
      ; UI presentation (id, color, etc.)



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
  :drag-items
  (fn [db [_ source]]
    (map #(get-in db [:drag-items %]) (get db source))))


(rf/reg-sub
  :subscriptions
  (fn [db _]
    (:subscriptions db)))

(rf/reg-sub
  :filters
  (fn [db [_ id]]
    (get-in db [:filters id])))

(rf/reg-sub
  :filter-drag-items
  (fn [db [_ id]]
    (if-let [filters (get-in db [:filters id])]
      (do
        (prn "found filters " filters)
        (->> filters
          (map #(get-in db [:drag-items %])))))))
          ;(map (juxt :id :name)))))))


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
;
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


  (def db @re-frame.db/app-db)



  (rf/dispatch [:add-source :generic-source #()])
  @(rf/subscribe [:drag-items :data-sources-list])

  (def id)

  (map #(get-in db [:drag-items %]) (get-in db [:filters id]))
  @(rf/subscribe [:filter-drag-items id])

  ())


; getting the id from the filter uuid
;
(comment
  (def id "55a1f781-3ae8-422d-b930-b083438b7644")
  (def db @re-frame.db/app-db)
  (def blank (:blank-widget db))


  (if-let [filters (get-in db [:filters id])]
    (do
      (prn "found filters " filters)
      (->> filters
        (map #(get-in db [:drag-items %])))))
        ;(map (juxt :id :name)))))




  ())