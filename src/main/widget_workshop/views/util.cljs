(ns widget-workshop.views.util)

(def nextId (atom 0))

(def colors
  [["blue" "white"]
   ["white" "black"]])

(defn- newColor []
  (nth colors (rand-int (count colors))))

(defn- nextName []
  (str "Widget " (swap! nextId inc)))

(defn make-new-widget [w]
  (let [[title-color text-color] (newColor)]
    {:id w
     :name (nextName)
     :title-color title-color
     :text-color text-color}))

