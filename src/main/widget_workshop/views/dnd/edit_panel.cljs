(ns widget-workshop.views.dnd.edit-panel)



(defn edit-panel [{:keys [step] :as item}]
  [:p.is-6 {:style {:color "lightgray"}}
   (str step)])


