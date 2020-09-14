(ns widget-workshop.views.oz.sankey
  (:require [oz.core :as oz]
            [reagent.core :as r]
            ["d3" :as d3]
            ["d3-sankey" :as d3-sankey]))

(defn make-data []
  {:nodes [{:node 0 :name "Oil"}
           {:node 1 :name "Coal"}
           {:node 2 :name "Renewable"}
           {:node 3 :name "Nuclear"}
           {:node 4 :name "Natural Gas"}
           {:node 5 :name "Transportation"}
           {:node 6 :name "Industrial"}
           {:node 7 :name "Residential & Commercial"}
           {:node 8 :name "Electric Power"}]
   :links [{:source "Oil" :target "Transportation" :value 94}
           {:source "Natural Gas" :target "Transportation" :value 3}
           {:source "Coal" :target "Transportation" :value 0}
           {:source "Renewable" :target "Transportation" :value 0}
           {:source "Nuclear" :target "Transportation" :value 3}
           {:source "Oil" :target "Industrial" :value 41}
           {:source "Natural Gas" :target "Industrial" :value 40}
           {:source "Coal" :target "Industrial" :value 7}
           {:source "Renewable" :target "Industrial" :value 11}
           {:source "Nuclear" :target "Industrial" :value 0}
           {:source "Oil" :target "Residential & Commercial" :value 17}
           {:source "Natural Gas" :target "Residential & Commercial" :value 76}
           {:source "Coal" :target "Residential & Commercial" :value 1}
           {:source "Renewable" :target "Residential & Commercial" :value 7}
           {:source "Nuclear" :target "Residential & Commercial" :value 0}
           {:source "Oil" :target "Electric Power" :value 1}
           {:source "Natural Gas" :target "Electric Power" :value 18}
           {:source "Coal" :target "Electric Power" :value 48}
           {:source "Renewable" :target "Electric Power" :value 11}
           {:source "Nuclear" :target "Electric Power" :value 22}]})

(defn make-sankey []
  [:div#sankey
    (d3/json (make-data) (fn [error graph]
                           (let [svg    (d3/select "#sankey")
                                 sankey (d3-sankey/sankey)]
                             (.. sankey
                                 (.nodes (.nodes graph))
                                 (.links (.links graph))
                                 (.layout a))
                             (set! (.nodes graph) sankey)
                             (set! (.links graph) (.links sankey))

                             )
                           )

    )])