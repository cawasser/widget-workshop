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
  [:div
   (if (not (empty? data))
     (let [ret (f/apply-filters pipeline data)]
       (cond
         (vector? ret) (map (fn [x] [:p (str x)]) ret)
         (map? ret) (map (fn [[k v]] [:p (str k ": " v)]) ret)
         :else (str ret)))
     [:p "empty"])])




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; get the 'steps' in the current-widget
(comment
  (def db @re-frame.db/app-db)
  (def widget @(rf/subscribe [:current-widget]))
  (def id (:id @(rf/subscribe [:current-widget])))

  (get-in db [:widgets id :steps])

  (def data @(rf/subscribe [:all-drag-items]))
  (def steps @(rf/subscribe [:widget-step-ids id]))

  (def data @(rf/subscribe [:all-drag-items]))


  (def id "2c155c4e-623e-4399-88c0-dc981b406f7f")
  (let [[data steps] [@(rf/subscribe [:all-drag-items])
                      @(rf/subscribe [:widget-step-ids id])]]
    (into []
      (map #(get-in data [% :step]) steps)))




  (def id (:id @(rf/subscribe [:current-widget])))
  (def data @(rf/subscribe [:widget-source-sample id]))
  (def pipeline @(rf/subscribe [:widget-pipeline id]))

  (f/apply-filters pipeline data)
  (map (fn [x] [:p (str x)]) (f/apply-filters pipeline data))

  ())



(comment
  (def id (:id @(rf/subscribe [:current-widget])))
  (def data @(rf/subscribe [:widget-source-sample id]))
  (def pipeline @(rf/subscribe [:widget-pipeline id]))

  [:div
   (if (not (empty? data))
     (map #([:p str %]) (f/apply-filters pipeline data))
     [:p "empty"])]


  (def ret (f/apply-filters pipeline data))

  (if (not (empty? data))
    (let [ret (f/apply-filters pipeline data)]
      (cond
        (vector? ret) (map (fn [x] [:p (str x)]) ret)
        (map? ret) (map (fn [[k v]] [:p (str k ": " v)]) ret)
        :else (str ret)))

    [:p "empty"])

  ())
