(ns widget-workshop.server.subscriptions
  (:require [re-frame.core :as rf]
            [widget-workshop.server.data-soup.data-soup]
            [widget-workshop.server.source.generic-data]
            [widget-workshop.server.source.config-data]
            [widget-workshop.server.data-soup.apply-filters :as f]
            [widget-workshop.util.uuid :refer [aUUID]]))





(defn- remove-drag-item [db id]
  "removes a drag item of the given id from the :drag-items key

  we need this function because the :drag-items content is actually keys by a UUID
  which is then associated with the id as it's value"

  [id]

  (dissoc (:builder/drag-items db)))

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
        :server/data-sources (assoc (:server/data-sources db) id source-fn)
        :builder/data-sources-list (conj (:builder/data-sources-list db) drag-id)
        :builder/drag-items (assoc (:builder/drag-items db)
                              drag-id {:id (aUUID) :type :source :name id})))))


(rf/reg-event-db
  :add-filter
  (fn [db [_ id dsl]]
    (let [drag-id (aUUID)]
      ;(prn ":add-filter" id dsl drag-id)
      (assoc db
        :builder/filter-source (assoc (:builder/filter-source db) id dsl)
        :builder/filter-list (conj (:builder/filter-list db) drag-id)
        :builder/drag-items (assoc (:builder/drag-items db)
                              drag-id {:id (aUUID) :type :filter :name id :filter dsl})))))


(rf/reg-event-db
  :remove-source
  (fn [db [_ id]]
    (assoc db
      :server/data-sources (dissoc (:builder/data-sources db) id)
      :builder/data-sources-list (disj (:builder/data-sources-list db) id)
      :builder/drag-items (remove-drag-item db id))))




(defn- pub-to-subscriber [db id results])
  ;(let [:steps  (get-in db [:builder/:steps id])
  ;      filtered (f/apply-:steps :steps (:live/data results))]
  ;  (assoc-in db
  ;    [:data id]
  ;    (assoc results :data filtered
  ;                   :keys (into [] (keys (first filtered)))))))


; HACK ALERT!
;
; 'publishes' the updated data from 'id' to the :data key in app-db
;  as 'stand-in' for actually publishing the data across the network between the
;  server and client. this would actually be run on the SERVER
;
;
; also, this function uses the :subscriptions to build a custom :data value
; unique to each subscriber (widget)
;
(rf/reg-event-db
  :publish
  (fn [db [_ id]]
    (let [source-fn (get-in db [:data-sources id])
          result    (source-fn)]
      (map #(pub-to-subscriber db % result) (get db :subscriptions)))))




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


(defn register-filter
  "registers a filter with a given identifier

  this identifier will be used later to (publish) filtered data
  from the associated source

  - id  - name used to uniquely identify the filter within the server
  - dsl - dsl (see apply-:steps ns) for the filter 'step'"

  [id dsl]

  (rf/dispatch [:add-filter id dsl]))


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

  (def db @re-frame.db/app-db)
  (def id "testing")
  (def dsl [:testing])
  (def source-sn #())


  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; publish some data
;
(comment
  (def db @re-frame.db/app-db)
  (def result ((get-in db [:builder/data-sources "generic-source"])))


  (rf/dispatch [:publish "generic-source"])

  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; publish to multiple subscribers, using their unique :steps
;
(comment
  (def db @re-frame.db/app-db)
  (def source "generic-source")
  (def id "b5ebdc17-b040-47de-92f1-e01ffe06baeb")

  (def results ((get-in db [:builder/data-sources source])))



  ())

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; can we create re-frame subscriptions on the fly?
;
;   OR
;
; should we just do the subscriptions only on the server? see BIFF:
;
;   https://youtu.be/oYwhrq8hDFo?t=1879
;
;  (update to the model data triggers a function that creates an updated subscription...)
;
(comment

  ())

