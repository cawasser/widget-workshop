(ns widget-workshop.handlers.dynamic-subscriptions
  (:require [re-frame.core :as rf]
            [widget-workshop.util.uuid :refer [aUUID]]
            [widget-workshop.util.vectors :refer [splice reorder]]
            [widget-workshop.handlers.scenarios :as s]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; app-db utility functions
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- get-item
  "returns the name (string) to be displayed for a given item in one of the vectors
  inside the app-db

  - db  - the app-db for convenience
  - idx - the numeric position of the given item in the given source vector

  returns the item to be displayed in the UI"

  [db source idx]
  (->> idx
    (nth (get db source))
    (get (:builder/drag-items db))))



(defn- get-source-item
  "returns the name (string) to be displayed for a given item in one of the vectors
  inside the app-db

  - db  - the app-db for convenience
  - idx - the numeric position of the given item in the given source vector

  returns the item to be displayed in the UI"

  [db source _]
  (->> (get-in db [:builder/source (s/strip-suffix source)])
    first
    (get (:builder/drag-items db))))



(defn- get-filter-item
  "returns a vector of the (filter) id and name of the item being dropped

  this data will be used to construct a NEW item inside the 'to' widget

  - db       - the app-db for convenience
  - from     - the id of the source vector for the dragged item
  - from-idx - the numeric position of the given item in the 'from' vector

  returns the map of details about the given dragged item found, keyed by the item's uuid"

  [db from idx]

  (if-let [filters (get-in db [:builder/filter-list])]
    (do
      ;(prn "get-filter-item " filters from idx)
      (->> (nth filters idx)
        (vector :builder/drag-items)
        (get-in db)))))


(defn- get-source-filter-item
  "returns a vector of the (filter) id and name of the item being dropped

  this data will be used to construct a NEW item inside the 'to' widget

  - db       - the app-db for convenience
  - from     - the id of the source vector for the dragged item
  - from-idx - the numeric position of the given item in the 'from' vector

  returns the map of details about the given dragged item found, keyed by the item's uuid"

  [db from idx]

  (if-let [filters (get-in db [:builder/filters from])]
    (do
      ;(prn "get-source-filter-item " filters from idx)
      (->> (nth filters idx)
        (vector :builder/drag-items)
        (get-in db)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; TODO: consider making all the 'dnd handling' functions multi-methods
;
; Drag & Drop situational handler functions
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- allow-drop?
  "disallow dropping the same item into the filters twice

  db - the current db for performance / simplicity
  name (string) - the name of the item being dropped
  filters (vector) - vector of filter IDs (uuids) already in the TO widget

  returns

  true - if the 'name' does NOT already exist in the filters list (indirectly)
  false - if 'name' already exists"
  [db name filters]

  ;(prn "drop-allowed?" name (map #(-> (get-in db [:builder/drag-items %]) :name) filters))
  (if (some #{name}
        (map #(-> (get-in db [:builder/drag-items %]) :name)
          filters))
    false
    true))


(defn- new-widget
  "the user wants to create a new widget based upon the source item dropped on
  the 'blank widget'

  1) move the 'blank' widget id (a guid) into the :builder/widget-list key
  2) create a new guid for a new 'blank' and place into the :blank-widget key"
  [db from from-idx to to-idx]

  (let [new-widget (aUUID)
        item       (get-item db from from-idx)
        new-uuid   (aUUID)]
    ;(prn "new-widget " from from-idx new-uuid name)
    (assoc db :builder/widget-list (conj (:builder/widget-list db) new-widget)
              :builder/source (assoc (:builder/source db) new-widget #{new-uuid})
              :builder/drag-items (assoc (:builder/drag-items db)
                                    new-uuid (assoc item :id new-uuid)))))



(defn- add-source-to-widget [db from from-idx to to-idx]
  "the user wants to replace the source on an existing widget"

  [db from from-idx to to-idx]

  (let [item               (get-item db from from-idx)
        new-uuid           (aUUID)
        existing-to-source (get-in db [:builder/source to])]
    ;(prn "add-source-to-widget " from from-idx new-uuid item to to-idx existing-to-source)
    (if (some #{(:id item)} existing-to-source)
      db
      (assoc db :builder/source (assoc (:builder/source db) to #{new-uuid})
                :builder/drag-items (assoc (:builder/drag-items db)
                                      new-uuid (assoc item :id new-uuid))))))



(defn- add-filter-to-widget [db from from-idx to to-idx]
  "the user wants to add more filters to an existing widget"

  [db from from-idx to to-idx]

  ;(prn "add-filter-to-widget BEFORE" from from-idx to to-idx)

  (let [item                (get-filter-item db from from-idx)
        new-uuid            (aUUID)
        existing-to-filters (get-in db [:builder/filters to])]

    ;(prn "add-filter-to-widget AFTER" from from-idx new-uuid item to to-idx)
    (if (allow-drop? db (:name item) existing-to-filters)
      (assoc db :builder/filters (assoc (:builder/filters db)
                                   to (splice existing-to-filters to-idx 0 new-uuid))
                :builder/drag-items (assoc (:builder/drag-items db)
                                      new-uuid (assoc item :id new-uuid)))
      db)))



;(defn- connect-widgets [db from from-idx to to-idx]
;  "the user wants to connect two widgets together using the item dropped on
;  the 'to widget'"
;
;  [db from from-idx to to-idx]
;
;  (let [{:keys [id name]} (get-source-filtered-item db from from-idx)
;        new-uuid            (aUUID)
;        existing-to-filters (get-in db [:builder/filters to])]
;    (prn "connect-widgets " from from-idx new-uuid name to to-idx)
;    (if (allow-drop? db name existing-to-filters)
;      (assoc db :builder/filters (assoc (:builder/filters db)
;                                   to (splice existing-to-filters to-idx 0 new-uuid))
;                :builder/drag-items (assoc (:builder/drag-items db)
;                                      new-uuid {:id new-uuid :name name}))
;      db)))



;(defn- new-widget-from-widget [db from from-idx to to-idx]
;  "the user wants to connect an existing widgets to a 'new' widget using the item
;  dropped on the 'blank widget'"
;
;  [db from from-idx to to-idx]
;
;  (let [new-widget (aUUID)
;        item       (get-source-item db from from-idx)
;        new-uuid   (aUUID)]
;    (prn "new-widget-from-widget" from from-idx new-uuid item)
;    (assoc db :builder/widget-list (conj (:builder/widgets-list db) new-widget)
;              :builder/source (assoc (:builder/source db) new-widget #{new-uuid})
;              :builder/drag-items (assoc (:builder/drag-items db)
;                                    new-uuid (assoc item :id new-uuid)))))



(defn- reorder-widget-filters
  "the user wants to reorder the filters in a widget"

  [db from from-idx to-idx]

  (let [item (get-source-filter-item db from from-idx)]
    ;(prn "reorder-widget-filters " from from-idx to-idx item)
    (assoc db :builder/filters (assoc (:builder/filters db)
                                 from (reorder (get-in db [:builder/filters from])
                                        from-idx to-idx)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; handle the drop events, in all their flavors
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- handle-drop-event
  "this function is called by the UI when the user drops something onto something else

  it looks at the various situations possible (what got dropped form where to where) and call the
  correct 'actual' handler function for the specific case

  - db     - the app-db, for convenience so we don't need to use (poor practice) rf/subscriptions here
  - from   - the dnd source the item is FROM, used to look into app-db and find specifics about the item
             being dropped
  - from-idx  - numeric index of the item which was taken from the FROM vector
  - to     - the dnd source the item being dropped ONM, used to look into app-db to find the data structure
             that needs to be modified
  - to-idx - numeric index of where the user wants the item in the TO vector

  returns - an updated app-db via 'modification' to the db parameter"

  [db from from-idx to to-idx]

  ;(prn "-handle-drop-event " from to (s/drop-scenario? from to))

  (condp = (s/drop-scenario? from to)
    ; nothing to do (eg, can't reorder the sources list)
    :do-nothing db

    ; reorder the 'filters' on a widget
    :reorder-filters (reorder-widget-filters db (s/strip-suffix from) from-idx to-idx)

    ; dropping from the sources onto a new widget
    :new-widget-from-source (new-widget db (keyword from) from-idx to to-idx)

    ; drop from an existing widget onto the 'new' widget
    ;:new-widget-from-widget (new-widget-form-widget db from from-idx to to-idx)

    ; drop from one widget to another
    ;:connect-widgets (connect-widgets db from from-idx to to-idx)

    ; drop new sources onto a widget (not a new widget)
    :add-source-to-widget (add-source-to-widget db (keyword from) from-idx to to-idx)

    ; drop new filter onto a widget (not a new widget)
    :add-filter-to-widget (add-filter-to-widget db (keyword from) from-idx to to-idx)

    ; drop filter from one widget onto another widget (not a new widget)
    ;:add-filter-from-widget-to-widget (add-filter-to-widget db from from-idx to to-idx)

    ; can't do anything else
    :default db))



(rf/reg-event-db
  :handle-drop-event
  (fn [db [_ from from-idx to to-idx]]
    ;(prn ":handle-drop-event " from to)
    (handle-drop-event db from from-idx to to-idx)))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; subscriptions to all the data items for
; drag and drop in the builder
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(rf/reg-sub
  :data-sources
  (fn [db _]
    (keys (:builder/data-sources db))))

(rf/reg-sub
  :data-sources-list
  (fn [db _]
    (:builder/data-sources-list db)))

(rf/reg-sub
  :source

  (fn [db [_ id]]
    (get-in db [:builder/source id])))

(rf/reg-sub
  :source-drag-items

  ; this subscription depends on 2 other subscriptions:
  ;
  ;  1) :source for the given widget, if this changes (add/remove) we
  ;         need to re-fire
  ;  2) any changes to the entire :all-drag-items key, if we add new drag-items
  ;         we may need to re-fire
  (fn [[_ id]]
    [(rf/subscribe [:source id]) (rf/subscribe [:all-drag-items])])

  ; now, instead of looking in the db, we look in the results of the 2 prereq
  ; subscriptions
  (fn [[source drag-items]]
    (if source
      (let [ret (map #(get drag-items %) source)]
        ;(prn "found source " source "//" drag-items "//" ret)
        ret)
      [])))


(rf/reg-sub
  :filter-source
  (fn [db _]
    (keys (:builder/filter-source db))))

(rf/reg-sub
  :filter-list
  (fn [db _]
    (:builder/filter-list db)))

(rf/reg-sub
  :all-drag-items
  (fn [db _]
    ;(prn "all-drag-items " (:drag-items db))
    (:builder/drag-items db)))

(rf/reg-sub
  :drag-items
  (fn [db [_ source]]
    ;(prn "drag-items " source)
    (map #(get-in db [:builder/drag-items %]) (get db source))))



(rf/reg-sub
  :filters
  (fn [db [_ id]]
    ;(prn "filters " id "//" (get-in db [:filters id]))
    (get-in db [:builder/filters id])))

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





;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; figure out some of the dnd conditions and actions
;
(comment
  (def from "data-sources-list")
  (def to "202c00c7-7b12-4292-aa98-264e45d3c46d")
  (def new-blank-widget (aUUID))
  (def item "generic-source")

  (= from to "builder/data-sources-list")


  @re-frame.db/app-db


  (and
    (= from "builder/data-sources-list")
    (= to widget-workshop.views.dnd.components/new-widget))


  (def db @re-frame.db/app-db)
  (assoc db :builder/widget-list (conj (:builder/widget-list db) to)
            :builder/filters (assoc (:builder/filters db) to [item]))



  (->> 0
    (nth (get db from))
    (get (:builder/drag-items db))
    :name)

  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; find the specific item from :filters that the user has dropped
;
(comment

  (def db @re-frame.db/app-db)
  (def from "228f87c6-3411-49bc-95be-f2904aa2e2ec")
  (def from-idx 0)

  (if-let [filters (get-in db [:builder/filters from])]
    (do
      ;(prn "get-source-filtered-item " filters)
      (->> (nth filters from-idx)
        (vector :builder/drag-items)
        (get-in db))))

  (get-source-filtered-item db from from-idx)

  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; find specific item in the :data-sources-list
;
(comment
  (def db @re-frame.db/app-db)
  (def from-idx 2)


  (->> from-idx
    (nth (get db :builder/data-sources-list))
    (get (:builder/drag-items db))
    :name)

  ())



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; reorder the filters in a given widget
;
(comment
  (def db @re-frame.db/app-db)

  (reorder (get-in db [:builder/filters from]) from-idx to-idx)

  ())



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; refuse duplicate filters
;
(comment
  (def db @re-frame.db/app-db)
  (def to "68adf0a3-e8a4-4ad5-8ac3-04b260006ead")
  (get-in db [:builder/filters to])

  (def existing-to-filters (map #(-> (get-in db [:builder/drag-items %]) :name)
                             (get-in db [:builder/filters to])))

  (if (some #{"generic-source"}
        (map #(-> (get-in db [:builder/drag-items %]) :name)
          (get-in db [:builder/filters to])))
    true
    false)

  (def name "take 5")
  (def filters (get-in db [:builder/filters to]))

  (map #(-> (get-in db [:builder/drag-items %]) :name)
    filters)

  (if (some #{name}
        (map #(-> (get-in db [:builder/drag-items %]) :name)
          filters))
    false
    true)

  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; getting an item form a dnd list
;
(comment
  (def db @re-frame.db/app-db)
  (def from :builder/filter-list)
  (def from-idx 0)

  (get-item db from from-idx)

  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; poking at some drag-item stuff
;
(comment
  (def drag-items @(rf/subscribe [:all-drag-items]))
  (def source "b030b8be-07f8-4241-9c56-ccff897d529c")

  (get drag-items source)

  (def id source)
  @(rf/subscribe [:source-drag-items id])

  ())

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; working out replacing 'source' for a build widget
;
(comment
  (def db @re-frame.db/app-db)
  (def from :builder/data-sources-list)
  (def to "6962d1b3-8ebe-404f-a32f-155a19f8c613")
  (def new-widget (aUUID))
  (def item (get-item db :builder/data-sources-list 0))
  (def new-uuid (aUUID))

  (assoc db
    :builder/widget-list (conj (:builder/widget-list db) new-widget)
    :builder/source (assoc (:builder/source db) new-widget #{new-uuid})
    :builder/drag-items (assoc (:builder/drag-items db)
                          new-uuid (assoc item :id new-uuid)))


  (new-widget db :builder/data-sources-list 0 "New" 0)


  (def existing-to-source (get-in db [:builder/source to]))
  (def to "e83ec36d-673c-4202-ba27-61d3aec9831f")
  (if (some #{(:id item)} existing-to-source)
    db
    (assoc db :builder/source (assoc (:builder/source db) to #{new-uuid})
              :builder/drag-items (assoc (:builder/drag-items db)
                                    new-uuid (assoc item :id new-uuid))))

  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; get a source item from an existing widget
;
(comment
  (def db @re-frame.db/app-db)
  (def idx 0)

  (get-source-item db from from-idx)

  (if-let [filters (get-in db [:builder/filter-list])]
    (do
      ;(prn "get-source-filtered-item " filters from idx)
      (->> (nth filters idx)
        (vector :builder/drag-items)
        (get-in db))))

  (get-source-filtered-item db from from-idx)

  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; more play with reordering filters
;
(comment
  (def db @re-frame.db/app-db)
  (def from (s/strip-suffix "1deb4300-07e3-456b-b714-e90530596d2c@filter"))
  (def from-idx 1)
  (def to-idx 0)

  (let [item (get-source-filter-item db from from-idx)]
    ;(prn "reorder-widget-filters " from from-idx to-idx item)
    (assoc db :builder/filters (assoc (:builder/filters db)
                                 from (reorder (get-in db [:builder/filters from])
                                        from-idx to-idx))))

  ())


