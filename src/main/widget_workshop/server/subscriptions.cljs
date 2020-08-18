(ns widget-workshop.server.subscriptions
  (:require [re-frame.core :as rf]
            [widget-workshop.server.source.generic-data]
            [widget-workshop.server.source.config-data]
            [widget-workshop.util.uuid :refer [aUUID]]))





(defn- remove-drag-item [db id]
  "removes a drag item of the given id form the :drag-items key

  we need this function because the :drag-items content is actualy keys by a UUID
  which is then associated with the id as it's value"

  [id]

  (dissoc (:drag-items db)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
;
; handler functions for 'managing subscriptions' and 'publishing'
; updates
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(rf/reg-event-db
  :add-source
  (fn [db [_ id source-fn]]
    (let [drag-id (aUUID)]
      (assoc db
        :data-sources (assoc (:data-sources db) id source-fn)
        :data-sources-list (conj (:data-sources-list db) drag-id)
        :drag-items (assoc (:drag-items db) drag-id {:id (aUUID) :name id})))))

(rf/reg-event-db
  :remove-source
  (fn [db [_ id]]
    (assoc db
      :data-sources (dissoc (:data-sources db) id)
      :data-sources-list (disj (:data-sources-list db) id)
      :drag-items (remove-drag-item db id))))




; HACK ALERT!
;
; 'publishes' the updated data from 'id' to the :data key in app-db
;  as 'stand-in' for actually publishing the data across the network between the
;  server and client
;
(rf/reg-event-db
  :publish
  (fn [db [_ id]]
    (let [source-fn (get-in db [:data-sources])])))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
;
; API functions
;
;    these separate the client from the 'hacks' we're using
;    to fake a remote server
;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



(defn register-data-source
  "registers a data source with a given (keyword) identifier

  this identifier will be used later to (publish) updated data
  from the associated source

  - id  - keyword used to uniquely identify the source within the server
  - source-fn - function to call which will return the moste up-to-date data
                for the source"

  [id source-fn]

  (rf/dispatch [:add-source id source-fn]))


(defn unregister-data-source
  "unregisters a data source with a given (keyword) identifier

  this identifier will be used later to (publish) updated data
  from the associated source

  - id  - keyword used to uniquely identify the source within the server"

  [id]

  (rf/dispatch [:remove-source id]))


(defn publish
  "HACK ALERT!

  this function 'publishes' the updated source data

  - id - keyword uniquely identifying the data-source to publish"

  [id]


  (rf/dispatch [:publish id]))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; flex the subscription-manager
;
(comment

  @re-frame.db/app-db

  (register-data-source
    "generic-source"
    widget-workshop.server.generic-data-source/get-data)



  (register-data-source :really-big-source #())
  (unregister-data-source :generic-source)
  (unregister-data-source :really-big-source)


  ((get-in @re-frame.db/app-db [:data-sources :generic-source]))

  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; can we create re-frame subscriptions on the fly?
;
(comment)

