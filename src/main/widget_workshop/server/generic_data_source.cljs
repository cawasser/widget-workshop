(ns widget-workshop.server.generic-data-source)


(defn get-data
  "by convention, this function returns the 'raw' data for a given source

  it is expected that this data will be further processed (filtered, computed, etc.)
  before actually being sent on to a client for rendering into a widget

  returns a vector of maps, one map for each data item"
  []

  [{:id 1 :x 100 :y 100 :datetime #inst"2020-08-11T10:00:00.000Z"}
   {:id 2 :x 200 :y 50 :datetime #inst"2020-08-11T11:00:00.000Z"}
   {:id 3 :x 300 :y 25 :datetime #inst"2020-08-11T12:00:00.000Z"}
   {:id 4 :x 400 :y 12.5 :datetime #inst"2020-08-11T13:00:00.000Z"}])



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; exercise the service a bit
;
(comment

  (get-data)

  ())
