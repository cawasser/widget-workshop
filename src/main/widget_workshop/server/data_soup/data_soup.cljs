(ns widget-workshop.server.data-soup.data-soup
  (:require [widget-workshop.server.source.util :refer [keyset]]
            [datascript.core :as d]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; bringing datascript into this
;
; the idea is goes back to "data soup": all the data is
; available all the time, tagged by entity (so we know EVERYTHING about
; a given entity), organized by WHEN that 'everything' is true (:datetime)
;
; ADVANTAGES:
;
;  1) having all the data accessible simplifies things because every query returns
;     ALL that is knowable
;
;  2) because the data is more or less uniform (everything must has :id and :datetime)
;     ALL views of data share this commonality, so they can be synced
;
;
; CONCERNS:
;
;  1) how to prevent realizing ALL the data, would prefer something
;     lazy, so we can work at a theoretical level?
;
;  2) how do we split the work between 'nodes' (ie, client and server(s))
;     since both need (some portion of) the data?
;
;  3) how do we implement the 'filtering sync' mechanism between UI components?
;
;  4) security?
;


(def schema {})
(def conn (d/create-conn schema))


(defn- symbolize [k] (symbol (str "?" (name k))))

(defn make-where
  "construct a :where clause from a keyword

  Assumes the entity will be boudn to '?e'"
  [k]
  [(symbol "?e") k (symbolize k)])


(defn make-binding
  "construct a :where clause from a keyword

  Assumes the entite will be boudn to '?e'"
  [binding k val]

  [:in (symbol "$") (symbolize k)])



(defn load-data
  "HACK: loads all the data into datascript

  this is only to support experimentation exclusively within CLJS and shadow-cljs

  see opening comments for more details

  returns a datascript transaction report, see
      https://cljdoc.org/d/datascript/datascript/1.0.0/api/datascript.core#transact!
  for details"

  []
  (->> [{:id 1 :x 100 :y 100 :datetime #inst"2020-08-11T10:00:00.000Z"}
        {:id 2 :x 200 :y 50 :datetime #inst"2020-08-11T11:00:00.000Z"}
        {:id 3 :x 300 :y 25 :datetime #inst"2020-08-11T12:00:00.000Z"}
        {:id 4 :x 400 :y 12.5 :datetime #inst"2020-08-11T13:00:00.000Z"}

        {:datetime #inst"2020-08-11T10:00:00.000Z" :id 1 :kind "alpha" :param-1 "off" :param-2 "off"}
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
        {:datetime #inst"2020-08-11T17:00:00.000Z" :id 5 :kind "beta" :x 140 :y 100}]

    (d/transact! conn)))






(comment
  (load-data)


  ; all entities (ie. anything with an :id)
  (d/q '[:find [(pull ?e [*]) ...]
         :where [?e :id _]]
    @conn)

  ; how can we make :where clauses from arbitrary attributes?
  (make-where :datetime)

  ; can we make query binding for 'passing in' values to a query?
  (make-binding "" :kind "alpha")

  ())


