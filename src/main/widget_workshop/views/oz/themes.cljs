(ns widget-workshop.views.oz.themes)


(def lightColor "#fff")
(def medColor "#888")

(def darkTheme {:background "#333"
                :title      {:color lightColor}
                :style      {"guide-label" {:fill lightColor}

                             "guide-title" {:fill lightColor}}

                :axis       {:domain-color lightColor
                             :grid-color   medColor
                             :tick-color   lightColor}})
;