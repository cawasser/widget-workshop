(ns widget-workshop.views.dnd.edit-panel)


(defn input-field [tag data isOpen?]
  (prn "input-field" tag data)
  [:div.field {:style {:color "lightgray"}
               :on-double-click #(do
                                   (prn "flipping edit off")
                                   (swap! isOpen? not))}
   [tag
    {:type      :number
     :value     data}]])
     ;:on-change #(prn (str (-> % .-target .-value)))}]])



(defn- numeric-value [item isOpen?]
  (prn "numeric-value" item (-> item :step second :value) @isOpen?)
  (fn []
    (if @isOpen?
      [input-field :input.input (-> item :step second :value) isOpen?]

      [:p.is-6 {:style {:color "lightgray"}
                :on-double-click #(do
                                    (prn "flipping edit on" item)
                                    (swap! isOpen? not))}
       (-> item :step second :value str)])))




(defn- vector-value [item isOpen?]
  [:p {:on-double-click #(swap! isOpen? not)}
   (str (keys (:param (second (:step item)))))])



(defn map-value [item isOpen?]
  (let [param (:param (second (:step item)))
        type (first (keys param))]
    (prn "map-value" param type)
    (cond
      (= type :vector) (vector-value item isOpen?)
      (= type :scalar) [numeric-value item isOpen?]
      :else [:p])))



(defn edit-panel [{:keys [step] :as item} isOpen?]
  (let [param (:param (second step))]
    (prn "edit-panel" item step param)
    (cond
      (map? param) (map-value item isOpen?)
      (= param :none) [:p]
      :else [:p (str param)])))





;;;;;;;;;;;;;;;;;;;;;;;;
; work out decoding the :step element of the item
;
(comment



  ())