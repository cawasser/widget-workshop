(ns widget-workshop.handlers.compose-widgets
  (:require [re-frame.core :as rf]
            [widget-workshop.util.uuid :refer [aUUID]]
            [widget-workshop.util.vectors :refer [splice reorder]]
            [widget-workshop.handlers.scenarios :as s]
            [widget-workshop.views.util :refer [make-new-widget]]))


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



(defn- get-step-item
  "returns a vector of the (step) id and name of the item being dropped

  this data will be used to construct a NEW item inside the 'to' widget

  - db       - the app-db for convenience
  - from     - the id of the source vector for the dragged item
  - from-idx - the numeric position of the given item in the 'from' vector

  returns the map of details about the given dragged item found, keyed by the item's uuid"

  [db from idx]

  (if-let [steps (get-in db [from])]
    (do
      ;(prn "get-filter-item " :steps from idx)
      (->> (nth steps idx)
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
      ;(prn "get-source-filter-item " :steps from idx)
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
  "disallow dropping the same item into the :steps twice

  db - the current db for performance / simplicity
  name (string) - the name of the item being dropped
  :steps (vector) - vector of filter IDs (uuids) already in the TO widget

  returns

  true - if the 'name' does NOT already exist in the :steps list (indirectly)
  false - if 'name' already exists"
  [db name filters]

  (prn "drop-allowed?" name filters (map #(-> (get-in db [:builder/drag-items %]) :name) filters))
  (if (some #{name}
        (map #(-> (get-in db [:builder/drag-items %]) :name)
          filters))
    false
    true))



(defn- replace-source
  "replace the current source for the current widget under construction

  NOTE: this current LEAKS drag-items (we never delete to old one)"
  [db widget new-uuid]
  (-> (get-in db [:widgets (:id widget)])
    (assoc :source new-uuid)))



(defn- add-source-to-widget [db from from-idx to to-idx]
  "the user wants to replace the source on an existing widget"

  [db from from-idx to to-idx]

  (let [item           (get-item db from from-idx)
        new-uuid       (aUUID)
        current-widget (:builder/current-widget db)
        widget         (get-in db [:widgets current-widget])]

    (prn "add-source-to-widget " from from-idx item to to-idx current-widget new-uuid)

    (if (some #{(:id item)} (:source current-widget))
      db
      (-> db
        (assoc-in [:widgets current-widget]
          (replace-source db widget new-uuid))
        (assoc-in [:builder/drag-items new-uuid]
          (assoc item :id new-uuid))))))


(defn- add-step [db widget new-uuid to-idx]
  (let [x (get-in db [:widgets (:id widget)])]
    (assoc x :steps (conj (:steps x) new-uuid))))



(defn- add-step-to-widget [db from from-idx to to-idx]
  "the user wants to add more :steps to an existing widget"

  [db from from-idx to to-idx]

  (prn "add-step-to-widget BEFORE" from from-idx to to-idx)

  (let [item           (get-step-item db from from-idx)
        ;current-widget @(rf/subscribe [:current-widget-id])
        widget         @(rf/subscribe [:current-widget])
        new-uuid       (aUUID)]

    (-> db
      (assoc-in [:widgets (:id widget)]
        (add-step db widget new-uuid to-idx))
      (assoc-in [:builder/drag-items new-uuid]
        (assoc item :id new-uuid)))))



(defn- reorder-widget-filters
  "the user wants to reorder the :steps in a widget"

  [db from from-idx to-idx]

  (let [item (get-source-filter-item db from from-idx)]
    ;(prn "reorder-widget-:steps " from from-idx to-idx item)
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

  (prn "handle-drop-event" from to (s/drop-scenario? from to))

  (condp = (s/drop-scenario? from to)
    ; nothing to do (eg, can't reorder the sources list)
    :do-nothing db

    ; drop new sources onto a widget (not a new widget)
    :add-source-to-widget (add-source-to-widget db (keyword from) from-idx to to-idx)

    ; drop new filter onto a widget (not a new widget)
    :add-step-to-widget (add-step-to-widget db (keyword from) from-idx to to-idx)

    ; reorder the ':steps' on a widget
    :reorder-filters (reorder-widget-filters db (s/strip-suffix from) from-idx to-idx)


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
    (prn "pre-subscription" id)
    [(rf/subscribe [:source id]) (rf/subscribe [:all-drag-items])])

  ; now, instead of looking in the db, we look in the results of the 2 prereq
  ; subscriptions
  (fn [[source drag-items]]
    (prn ":source-drag-items" source drag-items)
    (if source
      (let [ret (map #(get drag-items %) source)]
        ;(prn "found source " source "//" drag-items "//" ret)
        ret)
      [])))


;(rf/reg-sub
;  :filter-source
;  (fn [db _]
;    (keys (:builder/filter-source db))))
;
;(rf/reg-sub
;  :filter-list
;  (fn [db _]
;    (:builder/filter-list db)))

(rf/reg-sub
  :all-drag-items
  (fn [db _]
    ;(prn "all-drag-items " (:drag-items db))
    (:builder/drag-items db)))

(rf/reg-sub
  :drag-item
  (fn [db [_ id]]
    ;(prn "drag-item " id)
    (get-in db [:builder/drag-items id])))

(rf/reg-sub
  :drag-items
  (fn [db [_ source]]
    ;(prn "drag-item " id)
    (map #(get-in db [:builder/drag-items %]) (get db source))))


;(rf/reg-sub
;  :steps
;  (fn [db [_ id]]
;    ;(prn ":steps " id "//" (get-in db [::steps id]))
;    (get-in db [:builder/steps id])))
;
;(rf/reg-sub
;  :filter-drag-items
;
;  ; this subscription depends on 2 other subscriptions:
;  ;
;  ;  1) ::steps for the given widget, if this changes (add/remove/reorder) we
;  ;         need to re-fire
;  ;  2) any changes to the entire :all-drag-items key, if we add new drag-items
;  ;         we may need ot re-fire
;  (fn [[_ id]]
;    [(rf/subscribe [:steps id]) (rf/subscribe [:all-drag-items])])
;
;  ; now, instead of looking in the db, we look in the results of the 2 prereq
;  ; subscriptions
;  (fn [[filters drag-items]]
;    (if filters
;      (let [ret (map #(get drag-items %) filters)]
;        ;(prn "found :steps " :steps "//" drag-items "//" ret)
;        ret)
;
;      [])))
;



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; add/replace the source for the :current-widget
(comment
  (def db @re-frame.db/app-db)
  (def from :builder/sources-list)
  (def from-idx 0)
  (def item (get-item db from from-idx))
  (def new-uuid (aUUID))
  (def current-widget (:builder/current-widget db))



  (defn- replace-source
    "replace the current source for the current widget under construction

    NOTE: this current LEAKS drag-items (we never delete to old one)"
    [widget new-uuid]
    (-> (get-in db [:widgets (:id widget)])
      (assoc :source new-uuid)))

  (def widget (get-in db [:widgets current-widget]))
  (if (some #{(:id item)} (:source widget))
    db
    (-> db
      (assoc-in [:widgets current-widget]
        (replace-source widget new-uuid))
      (assoc-in [:builder/drag-items new-uuid]
        (assoc item :id new-uuid))))

  ())


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
; handle adding to current-widget's :step
(comment
  (def db @re-frame.db/app-db)
  (def new-uuid (aUUID))
  (def from :builder/steps-list)
  (def from-idx 0)
  (def to "builder/steps-tool")
  (def to-idx 0)
  (def current-widget (:builder/current-widget db))
  (def widget (get-in db [:widgets current-widget]))

  (let [x (get-in db [:widgets (:id widget)])]
    (assoc x :steps (conj (:steps x) new-uuid)))

  (:source widget)
  (:steps widget)
  (get-in db [:widgets current-widget])

  (def item           (get-step-item db from from-idx))

  (let [item           (get-step-item db from from-idx)
        widget         @(rf/subscribe [:current-widget])
        new-uuid       (aUUID)]

    (-> db
      (assoc-in [:widgets (:id widget)]
        (add-step db widget new-uuid to-idx))
      (assoc-in [:builder/drag-items new-uuid]
        (assoc item :id new-uuid))))
  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; find the specific item from :steps that the user has dropped
;
(comment
  (def db @re-frame.db/app-db)
  (def from :builder/steps-list)
  (def from-idx 3)
  (def idx 2)

  (if-let [steps (get-in db [from])]
    (do
      ;(prn "get-filter-item " :steps from idx)
      (->> (nth steps idx)
        (vector :builder/drag-items)
        (get-in db))))



  (get-step-item db from from-idx)

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
; reorder the :steps in a given widget
;
(comment
  (def db @re-frame.db/app-db)
  (def from "9e256d23-4107-4955-8853-136f18978879")

  (reorder (get-in db [:builder/filters from]) from-idx to-idx)

  ())



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; refuse duplicate :steps
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
; getting an item from a dnd list
;
(comment
  (def db @re-frame.db/app-db)
  (def from :builder/filter-list)
  (def from-idx 0)

  (get-item db from from-idx)
  (get-item db from 0)
  (get-item db from 1)
  (get-item db from 2)
  (get-item db from 3)

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
      ;(prn "get-source-filtered-item " :steps from idx)
      (->> (nth filters idx)
        (vector :builder/drag-items)
        (get-in db))))

  (get-source-filtered-item db from from-idx)

  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; more play with reordering :steps
;
(comment
  (def db @re-frame.db/app-db)
  (def from (s/strip-suffix "1deb4300-07e3-456b-b714-e90530596d2c@filter"))
  (def from-idx 1)
  (def to-idx 0)

  (let [item (get-source-filter-item db from from-idx)]
    ;(prn "reorder-widget-:steps " from from-idx to-idx item)
    (assoc db :builder/filters (assoc (:builder/filters db)
                                 from (reorder (get-in db [:builder/filters from])
                                        from-idx to-idx))))

  ())




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; reworking dropping sources and :steps for the BretVictor approach
;
(comment
  (def db @re-frame.db/app-db)
  (def current-widget (:builder/current-widget db))
  (def widget @(rf/subscribe [:widget current-widget]))

  @(rf/subscribe [:drag-items (:source widget)])

  ())