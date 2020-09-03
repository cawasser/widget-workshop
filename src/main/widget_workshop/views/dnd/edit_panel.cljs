(ns widget-workshop.views.dnd.edit-panel)



(defn- numeric-value [item]
  [:p.is-6 {:style {:color "lightgray"}}
   (str "number")])




(defn edit-panel [{:keys [step] :as item}]
  (let [param (second (:step item))]
    (str step)))





;;;;;;;;;;;;;;;;;;;;;;;;
; work out decoding the :step element of the item
;
(comment



  ())