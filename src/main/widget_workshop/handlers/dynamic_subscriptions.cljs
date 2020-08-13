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

(defn- get-source-item-name
  "returns the name (string) to be displayed for a given item in :data-sources
  inside the app-db

  - db   - the app-db for convenience
  - from-idx - the numeric position of the given item in the :data-sources vector

  returns the name (string) of the item to be displayed in the UI"

  [db from-idx]
  (->> from-idx
    (nth (get db :data-sources-list))
    (get (:drag-items db))
    :name))


(defn- get-source-filtered-item
  "returns a vector of the (source) id and name of the item being dropped

  this data will be used to construct a NEW item inside the 'to' widget

  - db       - the app-db for convenience
  - from     - the id of the source vector for the dragged item
  - from-idx - the numeric position of the given item in the 'from' vector

  returns the map of details about the given dragged item found, keyed by the item's uuid"

  [db from from-idx]

  (if-let [filters (get-in db [:filters from])]
    (do
      ;(prn "get-source-filtered-item " filters from-idx)
      (->> (nth filters from-idx)
        (vector :drag-items)
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


(defn- new-widget
  "the user wants to create a new widget based upon the soruce item dropped on
  the 'blank widget'

  1) move the 'blank' widget id (a guid) into the :widgets key
  2) create a new guid for a new 'blank' and place into the :blank-widget key"
  [db from from-idx to to-idx]

  (let [new-blank-widget (aUUID)
        current-blank    (:blank-widget db)
        name             (get-source-item-name db from-idx)
        new-uuid         (aUUID)]
    (prn "new-widget " from from-idx new-uuid name current-blank new-blank-widget)
    (assoc db :widgets (conj (:widgets db) to)
              :filters (assoc (:filters db) to [new-uuid])
              :drag-items (assoc (:drag-items db) new-uuid {:id new-uuid :name name})
              :blank-widget new-blank-widget)))



(defn- add-to-widget [db from from-idx to to-idx]
  "the user wants to add more sources to an existing widget"

  [db from from-idx to to-idx]

  (let [name                (get-source-item-name db from-idx)
        new-uuid            (aUUID)
        existing-to-filters (get-in db [:filters to])]
    ;(prn "add-to-widget " from from-idx new-uuid name to to-idx)
    (assoc db :filters (assoc (:filters db)
                         to (splice existing-to-filters to-idx 0 new-uuid))
              :drag-items (assoc (:drag-items db) new-uuid {:id new-uuid :name name}))))



(defn- connect-widgets [db from from-idx to to-idx]
  "the user wants to connect two widgets together using the item dropped on
  the 'to widget'"

  [db from from-idx to to-idx]

  (let [{:keys [id name]} (get-source-filtered-item db from from-idx)
        new-uuid            (aUUID)
        existing-to-filters (get-in db [:filters to])]
    ;(prn "connect-widgets " from from-idx new-uuid name to to-idx)
    (assoc db :filters (assoc (:filters db)
                         to (splice existing-to-filters to-idx 0 new-uuid))
              :drag-items (assoc (:drag-items db) new-uuid {:id new-uuid :name name}))))



(defn- connect-to-new-widget [db from from-idx to to-idx]
  "the user wants to connect an existing widgets to a 'new' widget using the item
  dropped on the 'blank widget'"

  [db from from-idx to to-idx]

  (let [new-blank-widget (aUUID)
        current-blank    (:blank-widget db)
        {:keys [id name]} (get-source-filtered-item db from from-idx)
        new-uuid         (aUUID)]
    ;(prn "connect-to-new-widget " from from-idx new-uuid name current-blank new-blank-widget)
    (assoc db :widgets (conj (:widgets db) to)
              :filters (assoc (:filters db) to [new-uuid])
              :drag-items (assoc (:drag-items db) new-uuid {:id new-uuid :name name})
              :blank-widget new-blank-widget)))



(defn- reorder-widget-filters
  "the user wants to reorder the filters in a widget"

  [db from from-idx to-idx]

  (let [{:keys [id name]} (get-source-filtered-item db from from-idx)]
    ;(prn "reorder-widget-filters " from from-idx to-idx name)
    (assoc db :filters (assoc (:filters db) from (reorder (get-in db [:filters from]) from-idx to-idx)))))



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
  - to-idx - numeric index of when the user wants the item in the TO vector

  returns - an updated app-db via 'modification' to the db parameter"

  [db from from-idx to to-idx]

  ;(prn "-handle-drop-event " from to (:blank-widget db))

  (prn "scenario? " (s/scenario? db from to))
  (condp = (s/scenario? db from to)
    ; can't reorder the sources list
    :do-nothing db

    ; reorder the 'filters' on a widget
    :reorder-filters (reorder-widget-filters db from from-idx to-idx)

    ; dropping from the sources onto a new widget
    :new-widget-from-source (new-widget db (keyword from) from-idx to to-idx)

    ; drop from an existing widget onto the 'new' widget
    :new-widget-from-widget (connect-to-new-widget db from from-idx to to-idx)

    ; drop from one widget to another
    :connect-widgets (connect-widgets db from from-idx to to-idx)

    ; drop new sources onto a widget (not a new widget)
    :add-source-to-widget (add-to-widget db (keyword from) from-idx to to-idx)

    ; can't do anything else
    :default db))





(rf/reg-event-db
  :handle-drop-event
  (fn [db [_ from from-idx to to-idx]]
    ;(prn ":handle-drop-event " from to)
    (handle-drop-event db from from-idx to to-idx)))





;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; figure out some of the dnd conditions and actions
;
(comment
  (def from "data-sources-list")
  (def to "202c00c7-7b12-4292-aa98-264e45d3c46d")
  (def new-blank-widget (aUUID))
  (def item "generic-source")

  (= from to "data-sources-list")


  @re-frame.db/app-db


  (and
    (= from "data-sources-list")
    (= to (:blank-widget @re-frame.db/app-db)))


  (def db @re-frame.db/app-db)
  (assoc db :widgets (conj (:widgets db) to)
            :filters (assoc (:filters db) to [item])
            :blank-widget new-blank-widget)



  (->> 0
    (nth (get db from))
    (get (:drag-items db))
    :name)

  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; find the specific item from :filters nthat the user has dropped
;
(comment

  (def db @re-frame.db/app-db)
  (def from "228f87c6-3411-49bc-95be-f2904aa2e2ec")
  (def from-idx 0)

  (if-let [filters (get-in db [:filters from])]
    (do
      ;(prn "get-source-filtered-item " filters)
      (->> (nth filters from-idx)
        (vector :drag-items)
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
    (nth (get db :data-sources-list))
    (get (:drag-items db))
    :name)

  ())



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; reorder the filters in a given widget
;
(comment
  (def db @re-frame.db/app-db)

  (reorder (get-in db [:filters from]) from-idx to-idx)

  ())
