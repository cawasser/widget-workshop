(ns widget-workshop.handlers.initialization
  (:require
    [re-frame.core :as rf]
    [widget-workshop.util.uuid :refer [aUUID]]
    [widget-workshop.util.vectors :refer [disjoin]]
    [widget-workshop.views.dnd.new-widget :refer [new-widget-id new-widget-context]]))


(def init-db
  {
   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ; BUILDER DATA
   ;
   ; :builder/* keys are used to support the 'builder' features, where
   ; authorized users can create new widgets by defining data-sources
   ; and filters

   ; list of :data-sources id's. should always contain the same uuids as
   ; :server/data-sources
   ;
   ; used to create the UI for dragging
   :builder/data-sources-list []

   ; a map to a set with a single data-source for this widget, this is where the data
   ; will come from (singleton per widget)
   :builder/source            {}

   ; a map of filters to the dsl used to actually perform the operation
   ; on a data set
   :builder/filter-source     {}

   ; a vector of :filter ids
   ;
   ; used to create the UI for dragging filters
   :builder/filter-list       []

   ; map of UUIDs to uniquely identify a draggable item, each mapped to {:id} which
   ; provides human-readable naming for the item
   ;
   ; used to create each draggable in the UI (sidebar or widgets)
   :builder/filters           {}

   ; uuids for widgets 'under construction' on the builder page
   ;
   ; used to generate the widgets in the widget panel 'builder' UI
   :builder/widget-list       []

   ; 'content' for :widgets 'under construction' on the builder page
   ;
   ; provides data like :name, :bar-color, :text-color, etc
   :builder/widgets           {new-widget-id new-widget-context}

   ; map of uuids to {:id <uuid> :name <name>} for each draggble, so they are uniquely identified
   ; throughout the entire app
   ;
   ; use to identify draggable items for use in the UI, as well as any data needed for
   ; UI presentation (id, color, etc.)
   :builder/drag-items        {}

   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ; LIVE DATA
   ;
   ; :live/* keys are used to support the 'dashboard' features, where
   ; all users can create dashboards from existing widgets created using the
   ; 'builder' functionality

   ;
   ;
   :live/data                 {}

   ; uuids for 'real' widgets
   ;
   ; used to generate the widgets in the widget panel UI
   :live/widgets              []

   ;
   ;
   :live/widget-layout        {}


   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ; SERVER DATA
   ;
   ; :server/* keys are used to support the 'server' features since this
   ; is a cljs-only experiment. these keys would be moved to a real CLJ
   ; server

   ; map of the data-sources, with id mapped to the function used to generate
   ; the most up-to-date data
   :server/data-sources       {}



   ; map of 'data-source' to map of the subscribers (:client, :widget)
   ;
   ; used to update clients when sources 'change'
   :server/subscriptions      {}})





(rf/reg-event-db
  :initialize
  (fn [db _]
    init-db))




(rf/reg-event-db
  :subscribe
  (fn [db [_ source-name]]
    ;(prn source-name)
    (if (contains? (:server/data-sources db) source-name)
      (assoc db :server/subscriptions (conj (:server/subscriptions db) source-name))
      db)))


(rf/reg-event-db
  :unsubscribe
  (fn [db [_ source-name]]
    (assoc db :server/subscriptions (disj (:server/subscriptions db) source-name))))


(rf/reg-event-db
  :data-update
  (fn [db [_ source-name source-data]]
    (assoc-in db [:live/data source-name] source-data)))




(defn- remove-filters [db id]
  (dissoc (get-in db [:builder/filters]) id))

(defn- remove-drag-items [db id]
  (apply dissoc (:builder/drag-items db) (get-in db [:builder/filters id])))

(rf/reg-event-db
  :remove-builder-widget
  (fn [db [_ id]]
    ;(prn "removing widget " id)
    (assoc db :builder/widget-list (disjoin (:builder/widget-list db) id)
              :builder/filters (remove-filters db id)
              :builder/drag-items (remove-drag-items db id))))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; subscription to main data elements
;


(rf/reg-sub
  :subscriptions
  (fn [db _]
    (:server/subscriptions db)))


(rf/reg-sub
  :data
  (fn [db _]
    (:live/data db)))


(rf/reg-sub
  :buildable-widgets
  (fn [db _]
    (:builder/widget-list db)))


(rf/reg-sub
  :buildable-widget
  (fn [db [_ id]]
    (get-in db [:builder/widgets id])))



(rf/reg-sub
  :widgets
  (fn [db _]
    (:live/widgets db)))

(rf/reg-sub
  :widget-layout
  (fn [db _]
    (:live/widget-layout db)))





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
                                          :data [1 2 3 4]}])
  (rf/dispatch [:data-update :intermediate-data {:name "intermediate-data"
                                                 :data {:item-1 [1 1 1 1]
                                                        :item-2 [2 2 2 2]}}])

  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; re-arranging the data-sources collection
;
(comment
  (defonce db {:builder/data-sources [:basic-data :intermediate-data :three]})
  (def ddb (:builder/data-sources db))

  (:builder/data-sources db)

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
  @(rf/subscribe [:drag-items :builder/data-sources-list])

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


  (if-let [filters (get-in db [:builder/filters id])]
    (do
      ;(prn "found filters " filters)
      (->> filters
        (map #(get-in db [:builder/drag-items %])))))
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
  (def drag-items @(rf/subscribe [:builder/all-drag-items]))


  (map #(get drag-items %) filters)
  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; remove widget and all associated data (:filters and :drag-items)
;
(comment

  (def db @re-frame.db/app-db)
  (def source "cffbbbc9-6228-4ed4-8eca-4e1b35c5fefe")
  (:builder/filters db)
  (get-in db [:builder/filters source])


  ; remove filters
  (dissoc (:builder/filters db) source)

  ; remove drag-items)
  (apply dissoc (:builder/drag-items db) (get-in db [:filters source]))



  ())
