(ns widget-workshop.handlers.dynamic-subscriptions
  (:require [re-frame.core :as rf]
            [widget-workshop.util.uuid :refer [aUUID]]))



(defn- get-source-item [db from-idx]
  (->> from-idx
    (nth (get db :data-sources-list))
    (get (:drag-items db))
    :name))


(defn- get-source-filtered-item
  "returns a vector of the (source) id and name of the item being dropped

  this data will be used to construct a NEW item inside the 'to' widget"

  [db from from-idx]

  (if-let [filters (get-in db [:filters from])]
    (do
      (prn "get-source-filtered-item " filters from-idx)
      (->> (nth filters from-idx)
        (vector :drag-items)
        (get-in db)))))



(defn splice [coll at d & n]
  (let [[a b] (split-at at coll)
        c (drop d b)
        x (if n (concat a n c) (concat a c))]
    (into [] x)))


(defn- new-widget
  "the user wants to create a new widget based upon the soruce item dropped on
  the 'blank widget'

  1) move the 'blank' widget id (a guid) into the :widgets key
  2) create a new guid for a new 'blank' and place into the :blank-widget key"
  [db from from-idx to to-idx]

  (let [new-blank-widget (aUUID)
        current-blank    (:blank-widget db)
        name             (get-source-item db from-idx)
        ;(->> from-idx
        ;  (nth (get db from))
        ;  (get (:drag-items db))
        ;  :name)
        new-uuid         (aUUID)]
    (prn "new-widget " from from-idx new-uuid name current-blank new-blank-widget)
    (assoc db :widgets (conj (:widgets db) to)
              :filters (assoc (:filters db) to [new-uuid])
              :drag-items (assoc (:drag-items db) new-uuid {:id new-uuid :name name})
              :blank-widget new-blank-widget)))



(defn- add-to-widget [db from from-idx to to-idx]
  "the user wants to add more sources to an existing widget"

  [db from from-idx to to-idx]

  (let [name                (get-source-item db from-idx)
        new-uuid            (aUUID)
        existing-to-filters (get-in db [:filters to])]
    (prn "add-to-widget " from from-idx new-uuid name to to-idx)
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
    (prn "connect-widgets " from from-idx new-uuid name to to-idx)
    (assoc db :filters (assoc (:filters db)
                         to (splice existing-to-filters to-idx 0 new-uuid))
              :drag-items (assoc (:drag-items db) new-uuid {:id new-uuid :name name}))))



(defn- connect-to-new-widget [db from from-idx to to-idx]
  "the user wants to connect and eixtsing widgets to a 'new' widget using the item
  dropped on the 'blank widget'"

  [db from from-idx to to-idx]

  (let [new-blank-widget    (aUUID)
        current-blank       (:blank-widget db)
        {:keys [id name]}   (get-source-filtered-item db from from-idx)
        new-uuid            (aUUID)]
    (prn "connect-to-new-widget " from from-idx new-uuid name current-blank new-blank-widget)
    (assoc db :widgets (conj (:widgets db) to)
              :filters (assoc (:filters db) to [new-uuid])
              :drag-items (assoc (:drag-items db) new-uuid {:id new-uuid :name name})
              :blank-widget new-blank-widget)))



(defn- handle-drop-event [db from from-idx to to-idx]
  (prn "-handle-drop-event " from to (:blank-widget db))

  (cond
    ; can't reorder the sources list
    (= from to "data-sources-list") (do (prn ">>>>> do nothing") db)

    ; dropping from the sources onto a new widget
    (and
      (= from "data-sources-list")
      (= to (:blank-widget db))) (new-widget db (keyword from) from-idx to to-idx)

    ; drop from an existing widget onto the 'new' widget
    (and
      (not= from "data-sources-list")
      (= to (:blank-widget db))) (connect-to-new-widget db from from-idx to to-idx)

    ; drop from one widget to another
    (and
      (not= from "data-sources-list")
      (not= to (:blank-widget db))) (connect-widgets db from from-idx to to-idx)

    ; reorder the 'filters' on a widget

    ; drop new sources onto a widget (not a new widget)
    (and
      (= from "data-sources-list")
      (not= to (:blank-widget db))) (add-to-widget db (keyword from) from-idx to to-idx)

    ; can't do anything else
    :default (do (prn ">>>>>>> default") db)))





(rf/reg-event-db
  :handle-drop-event
  (fn [db [_ from from-idx to to-idx]]
    (prn ":handle-drop-event " from to)
    (handle-drop-event db from from-idx to to-idx)))






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
      (prn "get-source-filtered-item " filters)
      (->> (nth filters from-idx)
        (vector :drag-items)
        (get-in db))))

  (get-source-filtered-item db from from-idx)

  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; find specifi item in the :data-sources-list
;
(comment
  (def db @re-frame.db/app-db)
  (def from-idx 2)


  (->> from-idx
    (nth (get db :data-sources-list))
    (get (:drag-items db))
    :name)

  ())