(ns widget-workshop.views.builder.sample-pipeline
  (:require [re-frame.core :as rf]
            [widget-workshop.server.data-soup.apply-filters :as f]))



(rf/reg-sub
  :widget-step-ids
  (fn [db [_ id]]
    (prn ":widget-step-ids" id)
    (if (not (empty? id))
      (get-in db [:widgets id :steps])
      [])))

(rf/reg-sub
  :widget-pipeline
  (fn [[_ id]]
    (prn "pre :widget-pipeline" id)
    [(rf/subscribe [:all-drag-items])
     (rf/subscribe [:widget-step-ids id])])

  (fn [[data steps]]
    (prn ":widget-pipeline (steps)" steps  " (data) " data)
    (into []
      (map #(get-in data [% :step]) steps))))




(defn run-pipeline [pipeline data]
  (prn "run-pipeline" data pipeline)
  (if (and
        (not (empty? data)))
        ;(not (empty? pipeline)))
    (str (f/apply-filters pipeline data))
    "empty"))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; get the 'steps' in the current-widget
(comment
  (def db @re-frame.db/app-db)
  (def widget @(rf/subscribe [:current-widget]))
  (def id (:builder/current-widget db))

  (get-in db [:widgets id :steps])
  (def steps @(rf/subscribe [:widget-step-ids id]))

  (def data @(rf/subscribe [:all-drag-items]))


  (def id "2c155c4e-623e-4399-88c0-dc981b406f7f")
  (let [[data steps] [@(rf/subscribe [:all-drag-items])
                      @(rf/subscribe [:widget-step-ids id])]]
    (into []
      (map #(get-in data [% :step]) steps)))


  @(rf/subscribe [:widget-pipeline id])


  ())


