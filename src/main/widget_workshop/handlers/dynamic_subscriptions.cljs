(ns widget-workshop.handlers.dynamic-subscriptions
  (:require [re-frame.core :as rf]
            [widget-workshop.util.uuid :refer [aUUID]]))



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
        current-blank (:blank-widget db)
        name (->> from-idx
               (nth (get db (keyword from)))
               (get (:drag-items db))
               :name)
        new-uuid (aUUID)]
    (prn "new-widget " from from-idx new-uuid name current-blank new-blank-widget)
    (assoc db :widgets (conj (:widgets db) to)
              :filters (assoc (:filters db) to [new-uuid])
              :drag-items (assoc (:drag-items db) new-uuid {:id new-uuid :name name})
              :blank-widget new-blank-widget)))


(defn- add-to-widget [db from from-idx to to-idx]
  db)


(defn- connect-widgets [db from from-idx to to-idx]
  db)


(defn- handle-drop-event [db from from-idx to to-idx]
  (prn "-handle-drop-event " from to (:blank-widget db))

  (cond
    (= from to "data-sources-list") (do (prn ">>>>> do nothing")
                                      db)

    (and
      (= from "data-sources-list")
      (= to (:blank-widget db))) (new-widget db from from-idx to to-idx)

    :default db))





(rf/reg-event-db
  :handle-drop-event
  (fn [db [_ from from-idx to to-idx]]
    (prn ":handle-drop-event " from to)
    (handle-drop-event db from from-idx to to-idx)))






(comment
  (def from "data-sources-list")
  (def to "202c00c7-7b12-4292-aa98-264e45d3c46d")
  (def new-blank-widget ( aUUID))
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