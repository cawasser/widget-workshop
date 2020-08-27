(ns widget-workshop.server.source.config-data
  (:require [widget-workshop.server.source.util :refer [keyset]]))



(defn raw-data
  "by convention, this function returns the 'raw' data for a given source

  it is expected that this data will be further processed (filtered, computed, etc.)
  before actually being sent on to a client for rendering into a widget

  returns a vector of maps, one map for each data item"

  []

  ; how 'flat' should a source be?
  ;
  ; by flat we mean how denormalized, as in 'should each map contain ALL relevant data?'
  ;
  ; should different 'kinds' of entities (ie, all things with a :datetime
  ; are in the same source) be in the same source? or should they be in different sources
  ; and share some common keys?

  [{:datetime #inst"2020-08-11T10:00:00.000Z" :id 1 :kind "alpha" :param-1 "off" :param-2 "off"}
   {:datetime #inst"2020-08-11T11:00:00.000Z" :id 1 :kind "alpha" :param-1 "off" :param-2 "on"}
   {:datetime #inst"2020-08-11T12:00:00.000Z" :id 1 :kind "alpha" :param-1 "on" :param-2 "off"}
   {:datetime #inst"2020-08-11T13:00:00.000Z" :id 1 :kind "alpha" :param-1 "on" :param-2 "on"}
   {:datetime #inst"2020-08-11T13:00:00.000Z" :id 1 :kind "alpha" :param-1 "off" :param-2 "off"}

   {:datetime #inst"2020-08-11T10:00:00.000Z" :id 2 :kind "alpha" :param-1 "on" :param-2 "on"}
   {:datetime #inst"2020-08-11T12:00:00.000Z" :id 2 :kind "alpha" :param-1 "on" :param-2 "off"}
   {:datetime #inst"2020-08-11T13:00:00.000Z" :id 2 :kind "alpha" :param-1 "on" :param-2 "on"}

   {:datetime #inst"2020-08-11T12:00:00.000Z" :id 3 :kind "alpha" :param-1 "of" :param-2 "on"}
   {:datetime #inst"2020-08-11T15:00:00.000Z" :id 3 :kind "alpha" :param-1 "on" :param-2 "off"}
   {:datetime #inst"2020-08-11T17:00:00.000Z" :id 3 :kind "alpha" :param-1 "on" :param-2 "on"}

   ; for example, :id 4 has different params than the other 3:
   {:datetime #inst"2020-08-11T12:00:00.000Z" :id 4 :kind "alpha 2" :param-1 "of" :param-3 "on"}
   {:datetime #inst"2020-08-11T15:00:00.000Z" :id 4 :kind "alpha 2" :param-1 "on" :param-3 "off"}
   {:datetime #inst"2020-08-11T17:00:00.000Z" :id 4 :kind "alpha 2" :param-1 "on" :param-3 "on"}

   ; for example, :id 5 is a completely different 'thing':
   {:datetime #inst"2020-08-11T12:00:00.000Z" :id 5 :kind "beta" :x 100 :y 100}
   {:datetime #inst"2020-08-11T15:00:00.000Z" :id 5 :kind "beta" :x 120 :y 100}
   {:datetime #inst"2020-08-11T17:00:00.000Z" :id 5 :kind "beta" :x 140 :y 100}])


(defn get-data
  "by convention, this function returns the data for a given source (:data) along with any
    necessary metadata, using keywords for the semantic meaning

    it is expected that this data will be further processed (filtered, computed, etc.)
    before actually being sent on to a client for rendering into a widget

    returns a map with the following keys:

    - :keys    - keywords for all the potential enters in each :data map
    - :data    - vector of maps, one map for each data item"

  []
  (let [raw (raw-data)]
    {:keys (keyset raw)
     :data raw}))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; exercise the service a bit
;
(comment

  (raw-data)
  (keyset (raw-data))

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


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; some thoughts on an 'information model' for user-defined-data-selection
;
(comment

  ;     [:select (nee query/filter) [<fn> OR? <keywords>]
  ;          - how sophisticated do we make this?
  ;
  ;     [:map <fn>]
  ;     [:take <int>]
  ;     [:reduce <fn>]
  ;     [:first]
  ;     [:last]
  ;     [:nth n]
  ;     [:extract (nee 'map juxt') [<keyword>]]
  ;     [:chain (nee 'map comp') [<fn>]]
  ;     [:group-by [<keyword>]]


  (get-data)

  ; let's try to make a data-structure (vector) of this:
  (->> (get-data)
    :data
    (group-by (juxt :id)))


  (def pipeline [[:group [:id]]])

  ; this DSL would turn into:
  (->> (get-data)
      :data
      (group-by (apply juxt [:id])))

  ; how do we process the DSL?
  (for [[cmd & params] pipeline]
    {:cmd cmd :params params})


  ; let's get more complex (from above):
  (->> (get-data)
    :data
    (map (juxt :datetime :id :param-2))
    (map #(zipmap [:datetime :id :param-2] %))
    (group-by :id))





  (def pipeline2 [[:extract [:datetime :id :param-2]]
                  [:group-by [:id]]])
  (->> (get-data)
    :data

    ; [:extract [:datetime :id :param-2]]
    (map (apply juxt (-> pipeline2 first second)))
    (map #(zipmap (-> pipeline2 first second) %))

    ; [:group-by [:id]]
    (group-by (apply juxt (-> pipeline2 second second))))

  ; this produces 'close' to the expected answer, except the key is a VECTOR!
  ; (because we used 'juxt') and not a single value
  ;
  ; FYI, looks like multi-methods are a good approach for some of this




  ; so, how can we compose our thread-last? remember, ->> is a MACRO!
  ;
  ; loop/recur?
  ;

  (defn- process [[cmd params] data]
    ;(prn {:cmd cmd :params params :data data})
    data)

  (process (first pipeline2) (:data (get-data)))



  (defn apply-filters [dsl data]
    (if (empty? dsl)
      data
      (recur (rest dsl) (process (first dsl) data))))

  (apply-filters pipeline2 (:data (get-data)))


  ())


