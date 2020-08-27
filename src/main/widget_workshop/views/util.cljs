(ns widget-workshop.views.util)

(defonce nextId (atom 0))

(def colors
  [["AliceBlue" "black"]
   ["AntiqueWhite" "black"]
   ["Aqua" "black"]
   ["Aquamarine" "black"]
   ["Azure" "black"]
   ["Beige" "black"]
   ["Bisque" "black"]
   ["Black" "white"]
   ["BlanchedAlmond" "black"]
   ["Blue" "white"]
   ["BlueViolet" "black"]
   ["Brown" "white"]
   ["BurlyWood" "black"]
   ["CadetBlue" "white"]
   ["Chartreuse" "white"]
   ["Chocolate" "white"]
   ["Coral" "white"]
   ["CornflowerBlue" "white"]
   ["Cornsilk" "white"]
   ["Crimson" "white"]
   ["Cyan" "white"]
   ["DarkBlue" "white"]
   ["DarkCyan" "white"]
   ["DarkGoldenRod" "white"]
   ["DarkGray" "white"]
   ["DarkGrey" "white"]
   ["DarkGreen" "white"]
   ["DarkKhaki" "white"]
   ["DarkMagenta" "white"]
   ["DarkOliveGreen" "white"]
   ["DarkOrange" "white"]
   ["DarkOrchid" "white"]
   ["DarkRed" "white"]
   ["DarkSalmon" "white"]
   ["DarkSeaGreen" "white"]
   ["DarkSlateBlue" "white"]
   ["DarkSlateGray" "white"]
   ["DarkSlateGrey" "white"]
   ["DarkTurquoise" "white"]
   ["DarkViolet" "white"]
   ["DeepPink" "white"]
   ["DeepSkyBlue" "white"]
   ["DimGray" "white"]
   ["DimGrey" "white"]
   ["DodgerBlue" "white"]
   ["FireBrick" "white"]
   ["FloralWhite" "white"]])

(defn- newColor []
  (nth colors (rand-int (count colors))))

(defn- nextName []
  (str "Widget " (swap! nextId inc)))

(defn make-new-widget [w]
  (let [[title-color text-color] (newColor)]
    {:id          w
     :name        (nextName)
     :title-color title-color
     :text-color  text-color}))

