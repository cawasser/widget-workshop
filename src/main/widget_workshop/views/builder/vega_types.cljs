(ns widget-workshop.views.builder.vega-types)


(def vega-types ["arc" "area" "bar" "box plot" "circle" "line"
                 "point" "rect" "square" "tick" "trail" "sankey"])

(def vega-types-config {"arc" {:ui {}}
                        "area" {:ui {}}
                        "bar" {:ui {}}
                        "box plot" {:ui {}}
                        "circle" {:ui {}}
                        "line" {:ui {}}
                        "point" {:ui {}}
                        "rect" {:ui {}}
                        "square" {:ui {}}
                        "tick" {:ui {}}
                        "trail" {:ui {}}

                         "sankey" {:ui {}}})


(defn vega-ui [type]
  (get-in vega-types-config [type :ui]))