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
   ["BlueViolet" "white"]
   ["Brown" "white"]
   ["BurlyWood" "black"]
   ["CadetBlue" "white"]
   ["Chartreuse" "black"]
   ["Chocolate" "white"]
   ["Coral" "white"]
   ["CornflowerBlue" "white"]
   ["Cornsilk" "black"]
   ["Crimson" "white"]
   ["Cyan" "black"]
   ["DarkBlue" "white"]
   ["DarkCyan" "white"]
   ["DarkGoldenRod" "black"]
   ["DarkGray" "white"]
   ["DarkGrey" "white"]
   ["DarkGreen" "white"]
   ["DarkKhaki" "black"]
   ["DarkMagenta" "white"]
   ["DarkOliveGreen" "white"]
   ["DarkOrange" "white"]
   ["DarkOrchid" "white"]
   ["DarkRed" "white"]
   ["DarkSalmon" "black"]
   ["DarkSeaGreen" "black"]
   ["DarkSlateBlue" "white"]
   ["DarkSlateGray" "white"]
   ["DarkSlateGrey" "white"]
   ["DarkTurquoise" "black"]
   ["DarkViolet" "white"]
   ["DeepPink" "white"]
   ["DeepSkyBlue" "black"]
   ["DimGray" "white"]
   ["DimGrey" "white"]
   ["DodgerBlue" "white"]
   ["FireBrick" "white"]
   ["FloralWhite" "black"]
   ["ForestGreen" "white"]
   ["Fuchsia" "white"]
   ["Gainsboro" "black"]
   ["GhostWhite" "black"]
   ["Gold" "black"]
   ["Goldenrod" "black"]
   ["Gray" "white"]
   ["Green" "white"]
   ["GreenYellow" "black"]
   ["Grey" "white"]
   ["Honeydew" "black"]
   ["HotPink" "white"]
   ["IndianRed" "white"]
   ["Indigo" "white"]
   ["Ivory" "black"]
   ["Khaki" "black"]
   ["Lavender" "black"]
   ["LavenderBlush" "black"]
   ["LawnGreen" "black"]
   ["LemonChiff" "black"]
   ["LightBlue" "black"]
   ["LightCoral" "white"]
   ["LightCyan" "black"]
   ["LightGoldenrodYellow" "black"]
   ["LightGray" "black"]
   ["LightGreen" "black"]
   ["LightGrey" "black"]
   ["LightPink" "black"]
   ["LightSalmon" "black"]
   ["LightSeaGreen" "black"]
   ["LightSkyBlue" "black"]
   ["LightSlateGray" "black"]
   ["LightSlateGrey" "black"]
   ["LightSteelBlue" "black"]
   ["LightYellow" "black"]
   ["Lime" "black"]
   ["LimeGreen" "black"]
   ["Linen" "black"]
   ["Magenta" "white"]
   ["Maroon" "white"]
   ["MediumAquamarine" "black"]
   ["MediumBlue" "white"]
   ["MediumOrchid" "white"]
   ["MediumPurple" "white"]
   ["MediumSeaGreen" "black"]
   ["MediumSlateBlue" "white"]
   ["MediumSpringGreen" "black"]
   ["MediumTurquoise" "black"]
   ["MediumVioletRed" "white"]
   ["MidnightBlue" "white"]
   ["MintCream" "black"]
   ["MistyRose" "black"]
   ["Moccasin" "black"]
   ["NavajoWhite" "black"]
   ["Navy" "white"]
   ["OldLace" "black"]
   ["Olive" "white"]
   ["OliveDrab" "white"]
   ["Orange" "black"]
   ["OrangeRed" "white"]
   ["Orchid" "white"]
   ["PaleGoldenrod" "black"]
   ["PaleGreen" "black"]
   ["PaleTurquoise" "black"]
   ["PaleVioletRed" "black"]
   ["PapayaWhip" "black"]
   ["PeachPuff" "black"]
   ["Peru" "black"]
   ["Pink" "black"]
   ["Plum" "black"]
   ["PowderBlue" "black"]
   ["Purple" "white"]
   ["Rebeccapurple" "white"]
   ["Red" "white"]
   ["RosyBrown" "white"]
   ["RoyalBlue" "white"]
   ["SaddleBrown" "white"]
   ["Salmon" "white"]
   ["SandyBrown" "white"]
   ["SeaGreen" "white"]
   ["Seashell" "black"]
   ["Sienna" "white"]
   ["Silver" "black"]
   ["SkyBlue" "black"]
   ["SlateBlue" "white"]
   ["SlateGray" "white"]
   ["SlateGrey" "white"]
   ["Snow" "black"]
   ["SpringGreen" "black"]
   ["SteelBlue" "white"]
   ["Tan" "black"]
   ["Teal" "white"]
   ["Thistle" "black"]
   ["Tomato" "white"]
   ["Turquoise" "black"]
   ["Violet" "white"]
   ["Wheat" "black"]
   ["White" "black"]
   ["WhiteSmoke" "black"]
   ["Yellow" "black"]
   ["YellowGreen" "black"]])

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

