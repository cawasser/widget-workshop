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

(def vega-types-config {"arc"      {:ui x-y-chart-ui :base x-y-chart-base}
                        "area"     {:ui x-y-chart-ui :base x-y-chart-base}
                        "bar"      {:ui x-y-chart-ui :base x-y-chart-base}
                        "box plot" {:ui {} :base {}}
                        "circle"   {:ui x-y-chart-ui :base x-y-chart-base}
                        "line"     {:ui x-y-chart-ui :base x-y-chart-base}
                        "point"    {:ui x-y-chart-ui :base x-y-chart-base}
                        "rect"     {:ui x-y-chart-ui :base x-y-chart-base}
                        "square"   {:ui x-y-chart-ui :base x-y-chart-base}
                        "tick"     {:ui {} :base {}}
                        "trail"    {:ui {} :base {}}
                        "sankey"   {:ui {} :base {}}})


(defn vega-ui [type]
  (get-in vega-types-config [type :ui]))