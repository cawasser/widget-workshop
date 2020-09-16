(ns widget-workshop.views.builder.vega-types)


(def x-y-chart-base {:encoding {:x     {:field "" :type "quantitative"}
                                :y     {:field "" :type "quantitative"}
                                :color {:field "" :type "nominal"}}
                     :autosize {:type     "fit"
                                :contains "padding"}})

(def x-y-chart-ui {:encoding {:x     {:title "X"}
                              :y     {:title "y"}
                              :color {:title "Color"}}
                   :autosize {:type     "fit"
                              :contains "padding"}})

(def vega-types ["arc" "area" "bar" "box plot" "circle" "line"
                 "point" "rect" "square" "tick" "trail" "sankey"])

(def vega-types-config {"arc"      {:ui x-y-chart-ui :base x-y-chart-base :mark "arc"}
                        "area"     {:ui x-y-chart-ui :base x-y-chart-base :mark "area"}
                        "bar"      {:ui x-y-chart-ui :base x-y-chart-base :mark "bar"}
                        "box plot" {:ui x-y-chart-ui :base x-y-chart-base :mark "boxplot"}
                        "circle"   {:ui x-y-chart-ui :base x-y-chart-base :mark "circle"}
                        "line"     {:ui x-y-chart-ui :base x-y-chart-base :mark "line"}
                        "point"    {:ui x-y-chart-ui :base x-y-chart-base :mark "point"}
                        "rect"     {:ui x-y-chart-ui :base x-y-chart-base :mark "rect"}
                        "square"   {:ui x-y-chart-ui :base x-y-chart-base :mark "square"}
                        "tick"     {:ui x-y-chart-ui :base x-y-chart-base  :mark "tick"}
                        "trail"    {:ui x-y-chart-ui :base x-y-chart-base  :mark "trail"}
                        "sankey"   {:ui {} :base {} :mark "sankey"}})


(defn vega-ui [type]
  (get-in vega-types-config [type :ui]))
