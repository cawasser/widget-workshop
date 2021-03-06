(ns widget-workshop.views.oz.content
  (:require [oz.core :as oz]
            [reagent.core :as r]
            [re-frame.core :as rf]))


(defn play-data [& names]
  (for [n names
        i (range 20)]
    {:time i :item n :quantity (+ (Math/pow (* i (count n)) 0.8) (rand-int (count n)))}))

(def plot
  (r/atom {:data     {:values '()}
           :encoding {:x     {:field :datetime :type "quantitative"}
                      :y     {:field :x :type "quantitative"}
                      :color {:field :y :type "nominal"}}
           :autosize {:type     "fit"
                      :contains "padding"}}))
;:autosize {:type     "fit"
;           :contains "content"
;           :resize   true}}))



(defn compose-vega-input [widget after]
  (prn "compose-vega-plot" widget after)
  (let [ret (-> @plot
              (assoc-in [:data :values] after)
              (assoc :mark @(rf/subscribe [:widget-content-type (:id widget)])))]
    (prn "compose-vega-plot ret" ret)
    ret))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; for playing with oz at the shadow/repl
;


;;;;;;;;;;;;
; change the rand qty for the existing labels (monkey, etc)
;
(defn new-data [data]
  (let [old-data   @data
        old-values (get-in @data [:data :values])]
    (reset! data
      (-> old-data
        (assoc-in [:data :values] (play-data "monkey" "slipper" "broom"))))))


;;;;;;;;;;;;
; swap :x and :y axis data (see the chart update live!)
(defn swap-x-y [data]
  (let [old-data @data
        old-x    (get-in @data [:encoding :x])
        old-y    (get-in @data [:encoding :y])]
    (reset! data
      (-> old-data
        (assoc-in [:encoding :x] old-y)
        (assoc-in [:encoding :y] old-x)))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; play with the data for the widget
;
(comment
  (def db @re-frame.db/app-db)
  (def data (r/atom {:data     {:values (play-data "monkey" "slipper" "broom")}
                     :encoding {:x     {:field "time" :type "quantitative"}
                                :y     {:field "quantity" :type "quantitative"}
                                :color {:field "item" :type "nominal"}}
                     :mark     "line"}))
  (def old-x (get-in @data [:encoding :x]))
  (def old-y (get-in @data [:encoding :y]))

  (swap-x-y data)
  (swap-x-y line-plot)



  (def data (r/atom {:autosize {:type     "fit"
                                :contains "padding"}

                     :data     {:values (play-data "monkey" "slipper" "broom")}
                     :encoding {:x     {:field "time" :type "quantitative"}
                                :y     {:field "quantity" :type "quantitative"}
                                :color {:field "item" :type "nominal"}}
                     :mark     "line"}))
  (def old-data @data)
  (def old-values (get-in @data [:data :values]))

  (new-data data)
  (new-data line-plot)



  ())