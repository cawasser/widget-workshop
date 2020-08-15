(ns widget-workshop.server.config-data-source)

(defn get-data
  "by convention, this function returns the 'raw' data for a given source

  it is expected that this data will be further processed (filtered, computed, etc.)
  before actually being sent on to a client for rendering into a widget

  returns a vector of maps, one map for each data item"
  []

  [{:datetime #inst"2020-08-11T10:00:00.000Z" :id 1 :param-1 "off" :param-2 "off"}
   {:datetime #inst"2020-08-11T11:00:00.000Z" :id 1 :param-1 "off" :param-2 "on"}
   {:datetime #inst"2020-08-11T12:00:00.000Z" :id 1 :param-1 "on" :param-2 "off"}
   {:datetime #inst"2020-08-11T13:00:00.000Z" :id 1 :param-1 "on" :param-2 "on"}
   {:datetime #inst"2020-08-11T13:00:00.000Z" :id 1 :param-1 "off" :param-2 "off"}

   {:datetime #inst"2020-08-11T10:00:00.000Z" :id 2 :param-1 "on" :param-2 "on"}
   {:datetime #inst"2020-08-11T12:00:00.000Z" :id 2 :param-1 "on" :param-2 "off"}
   {:datetime #inst"2020-08-11T13:00:00.000Z" :id 2 :param-1 "on" :param-2 "on"}

   {:datetime #inst"2020-08-11T12:00:00.000Z" :id 3 :param-1 "of" :param-2 "on"}
   {:datetime #inst"2020-08-11T15:00:00.000Z" :id 3 :param-1 "on" :param-2 "off"}
   {:datetime #inst"2020-08-11T17:00:00.000Z" :id 3 :param-1 "on" :param-2 "on"}])




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; exercise the service a bit
;
(comment

  (get-data)


  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; play with transformations
;

(comment
  ; by entity
  (group-by :id (get-data))

  ; epochal (by datetime)
  (group-by :datetime (get-data))


  ; look just at :param-2 values (along with :id)
  (map (juxt :datetime :id :param-2) (get-data))

  (->> (get-data)
    (map (juxt :datetime :id :param-2))
    (group-by first))


  ; use a 'param' to determine the fields to extract and the field to
  ; group-by
  (def extract [:datetime :id :param-2])
  (def by :id)
  (->> (get-data)
    (map (apply juxt extract))
    (map #(zipmap extract %))
    (group-by by))

  (defn xform [data extract by]
    (->> data
      (map (apply juxt extract))
      (map #(zipmap extract %))
      (group-by by)))

  (xform (get-data) [:datetime :id :param-2] :datetime)
  (xform (get-data) [:datetime :id :param-2] :id)


  (def extract [:datetime :id :param-2])
  (def by [:datetime :id])
  (->> (get-data)
    (map (apply juxt extract))
    (map #(zipmap extract %))
    (group-by (apply juxt by)))


  ())