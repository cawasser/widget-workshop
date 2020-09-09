(ns widget-workshop.views.dnd.edit-panel
  (:require [re-frame.core :as rf]))



(rf/reg-sub
  :step
  (fn [db [_ id]]
    (-> db
      :builder/drag-items
      (get id))))




(rf/reg-sub
  :step-value
  (fn [db [_ id]]
    (-> db
      :builder/drag-items
      (get id)
      :step
      second
      :value)))


(rf/reg-event-db
  :update-step
  (fn [db [_ id new-value]]
    (prn ":update-step" id new-value)
    (let [[orig-step orig-param] (get-in db [:builder/drag-items id :step])]
      (assoc-in db [:builder/drag-items id :step]
        [orig-step (assoc orig-param :value new-value)]))))



(defn vector-field
  "input-field needs the 'id' to the specific step to be shown/edited"

  [tag item isOpen?]

  (fn []
   (let [value @(rf/subscribe [:step-value (:id item)])]
     (prn "vector-field" item (:id item) value)
     [:div.field
        {:on-double-click #(do
                             (rf/dispatch-sync
                               [:update-step (:id item) (-> % .-target .-value)])
                             ;(prn "flipping edit off")
                             (swap! isOpen? not))}
      [tag
       {:type      :text
        :value     (if (empty? value)
                     ""
                     (apply str value))
        :on-change #(rf/dispatch-sync
                      [:update-step (:id item) (-> % .-target .-value)])}]])))



(defn number-field
  "input-field needs the 'id' to the specific step to be shown/edited"

  [tag item isOpen?]

  (fn []
    ;(prn "input-field" item (:id item))
    [:div.field
     {:on-double-click #(do
                          (rf/dispatch-sync [:update-step (:id item)
                                             (js/parseInt (-> % .-target .-value))])
                          ;(prn "flipping edit off")
                          (swap! isOpen? not))}
     [tag
      {:type      :number
       :value     @(rf/subscribe [:step-value (:id item)])
       :on-change #(rf/dispatch-sync [:update-step (:id item)
                                      (js/parseInt (-> % .-target .-value))])}]]))



;(defn- numeric-value [item isOpen?]
;  (fn []
;    (prn "numeric-value" item @isOpen?)
;
;    (if @isOpen?
;      [number-field :input.input item isOpen?]
;
;      [:p.is-6 {:style {:color "lightgray"
;                        :font-weight :bold}
;                :on-double-click #(do
;                                    ;(prn "flipping edit on" item @(rf/subscribe [:step-value (:id item)]))
;                                    (swap! isOpen? not))}
;       @(rf/subscribe [:step-value (:id item)])])))
;



;(defn- vector-value [item isOpen?]
;  (fn []
;    (prn "vector-value" item @isOpen?)
;
;    (if @isOpen?
;      [vector-field :input.input item isOpen?]
;
;      [:p {.is-6 {:style {:color "lightgray"
;                          :font-weight :bold}
;                  :on-double-click #(swap! isOpen? not)}}
;       @(rf/subscribe [:step-value (:id item)])])))



(defn map-value [item isOpen?]
  (let [step  (second (:step item))
        param (:param step)
        type  (first (keys param))]
    (prn "map-value" param type (:value step))
    (fn []
      (if @isOpen?
        (cond
          (= type :vector) [vector-field :input.input item isOpen?]
          (= type :scalar) [number-field :input.input item isOpen?]
          :else [:p "dummy"])

        [:p.is-6 {:style           {:color       "lightgray"
                                    :font-weight :bold}
                  :on-double-click #(swap! isOpen? not)}
         (str @(rf/subscribe [:step-value (:id item)]))]))))





(defn edit-panel [{:keys [step] :as item} isOpen?]
  (fn []
    (let [param (:param (second step))]
      (prn "edit-panel" item step param)
      (cond
        (map? param) (map-value @(rf/subscribe [:step (:id item)]) isOpen?)
        (= param :none) [:p]
        :else [:p (str param)]))))





;;;;;;;;;;;;;;;;;;;;;;;;
; work out decoding the :step element of the item
;
(comment
  (def db @re-frame.db/app-db)
  (def id "d8553dfe-536b-43f9-b07a-24c686e45fc6")

  (-> db
    :builder/drag-items
    (get id)
    :step
    second
    :value)


  (let [[orig-step orig-param] (get-in db [:builder/drag-items id :step])]
    [orig-step (assoc orig-param :value "testing")])


  (let [[orig-step orig-param] (get-in db [:builder/drag-items id :step])]
    (assoc-in db [:builder/drag-items id :step]
      [orig-step (assoc orig-param :value "testing")]))

  ())