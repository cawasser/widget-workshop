(ns widget-workshop.handlers.initialization
  (:require
    [re-frame.core :as rf]
    [widget-workshop.util.uuid :refer [aUUID]]
    [widget-workshop.util.vectors :refer [disjoin]]))



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
    ;(prn source-name)
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




(defn- remove-filters [db id]
  (dissoc (:filters db) id))

(defn- remove-drag-items [db id]
  (apply dissoc (:drag-items db) (get-in db [:filters id])))

(rf/reg-event-db
  :remove-widget
  (fn [db [_ id]]
    ;(prn "removing widget " id)
    (assoc db :widgets (disjoin (:widgets db) id)
              :filters (remove-filters db id)
              :drag-items (remove-drag-items db id))))




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
  :all-drag-items
  (fn [db _]
    ;(prn "all-drag-items " (:drag-items db))
    (:drag-items db)))

(rf/reg-sub
  :drag-items
  (fn [db [_ source]]
    ;(prn "drag-items " source)
    (map #(get-in db [:drag-items %]) (get db source))))


(rf/reg-sub
  :subscriptions
  (fn [db _]
    (:subscriptions db)))

(rf/reg-sub
  :filters
  (fn [db [_ id]]
    ;(prn "filters " id "//" (get-in db [:filters id]))
    (get-in db [:filters id])))

(rf/reg-sub
  :filter-drag-items

  ; this subscription depends on 2 other subscriptions:
  ;
  ;  1) :filters for the given widget, if this changes (add/remove/reorder) we
  ;         need to re-fire
  ;  2) any changes to the entire :all-drag-items key, if we add new drag-items
  ;         we may need ot re-fire
  (fn [[_ id]]
    [(rf/subscribe [:filters id]) (rf/subscribe [:all-drag-items])])

  ; now, instead of looking in the db, we look in the results of the 2 prereq
  ; subscriptions
  (fn [[filters drag-items]]
    (if filters
      (let [ret (map #(get drag-items %) filters)]
        ;(prn "found filters " filters "//" drag-items "//" ret)
        ret)

      [])))


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


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
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


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; getting the id from the filter uuid
;
(comment
  (def id "55a1f781-3ae8-422d-b930-b083438b7644")
  (def db @re-frame.db/app-db)
  (def blank widget-workshop.views.dnd.components/new-widget)


  (if-let [filters (get-in db [:filters id])]
    (do
      ;(prn "found filters " filters)
      (->> filters
        (map #(get-in db [:drag-items %])))))
        ;(map (juxt :id :name)))))

  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; getting the drag-items for a given widget's filter
;
(comment

  @re-frame.db/app-db

  (def filters
    @(rf/subscribe [:filters
                    "b89aaa52-e4db-43f6-aedb-8324233d6a5a"]))
  (def drag-items @(rf/subscribe [:all-drag-items]))


  (map #(get drag-items %) filters)
  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; remove widget and all associated data (:filters and :drag-items)
;
(comment

  (def db @re-frame.db/app-db)
  (def source "cffbbbc9-6228-4ed4-8eca-4e1b35c5fefe")
  (:filters db)
  (get-in db [:filters source])


  ; remove filters
  (dissoc (:filters db) source)

  ; remove drag-items)
  (apply dissoc (:drag-items db) (get-in db [:filters source]))



  ())

