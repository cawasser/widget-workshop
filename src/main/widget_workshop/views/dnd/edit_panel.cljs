(ns widget-workshop.views.dnd.edit-panel)



(defn- numeric-value [item]
  (prn "numeric-value" item (-> item :step second :value))
  [:p.is-6 {:style {:color "lightgray"}}
   (-> item :step second :value str)])



(defn- vector-value [item]
  [:p (str (keys (:param (second (:step item)))))])



(defn map-value [item]
  (let [param (:param (second (:step item)))
        type (first (keys param))]
    (prn "map-value" param type)
    (cond
      (= type :vector) (vector-value item)
      (= type :scalar) (numeric-value item)
      :else [:p])))



(defn edit-panel [{:keys [step] :as item}]
  (let [param (:param (second step))]
    (prn "edit-panel" item step param)
    (cond
      (map? param) (map-value item)
      (= param :none) [:p]
      :else [:p (str param)])))





;;;;;;;;;;;;;;;;;;;;;;;;
; work out decoding the :step element of the item
;
(comment



  ())