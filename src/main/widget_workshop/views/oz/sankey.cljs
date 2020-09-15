(ns widget-workshop.views.oz.sankey
  (:require [oz.core :as oz]
            [reagent.core :as r]))


(def sankey-data
    {:height 500
     :width 900
     :data [{:name "rawData",
             :values {:aggregations {:table {:buckets [{:key {:stk1 "aa", :stk2 "11"}, :doc_count 7}
                                                       {:key {:stk1 "aa", :stk2 "111"}, :doc_count 4}
                                                       {:key {:stk1 "bb", :stk2 "1111"}, :doc_count 8}
                                                       {:key {:stk1 "cc", :stk2 "11"}, :doc_count 3}]}}},
             :format {:property "aggregations.table.buckets"},
             :transform [{:type "formula", :expr "datum.key.stk1", :as "stk1"}
                         {:type "formula", :expr "datum.key.stk2", :as "stk2"}
                         {:type "formula", :expr "datum.doc_count", :as "size"}]}
            {:name "nodes",
             :source "rawData",
             :transform [{:type "filter",
                          :expr "!groupSelector || groupSelector.stk1 == datum.stk1 || groupSelector.stk2 == datum.stk2"}
                         {:type "formula", :expr "datum.stk1+datum.stk2", :as "key"}
                         {:type "fold", :fields ["stk1" "stk2"], :as ["stack" "grpId"]}
                         {:type "formula",
                          :expr "datum.stack == 'stk1' ? datum.stk1+' '+datum.stk2 : datum.stk2+' '+datum.stk1",
                          :as "sortField"}
                         {:type "stack", :groupby ["stack"], :sort {:field "sortField", :order "descending"}, :field "size"}
                         {:type "formula", :expr "(datum.y0+datum.y1)/2", :as "yc"}]}
            {:name "groups",
             :source "nodes",
             :transform [{:type "aggregate", :groupby ["stack" "grpId"], :fields ["size"], :ops ["sum"], :as ["total"]}
                         {:type "stack", :groupby ["stack"], :sort {:field "grpId", :order "descending"}, :field "total"}
                         {:type "formula", :expr "scale('y', datum.y0)", :as "scaledY0"}
                         {:type "formula", :expr "scale('y', datum.y1)", :as "scaledY1"}
                         {:type "formula", :expr "datum.stack == 'stk1'", :as "rightLabel"}
                         {:type "formula", :expr "datum.total/domain('y')[1]", :as "percentage"}]}
            {:name "destinationNodes", :source "nodes", :transform [{:type "filter", :expr "datum.stack == 'stk2'"}]}
            {:name "edges",
             :source "nodes",
             :transform [{:type "filter", :expr "datum.stack == 'stk1'"}
                         {:type "lookup", :from "destinationNodes", :key "key", :fields ["key"], :as ["target"]}
                         {:type "linkpath",
                          :orient "horizontal",
                          :shape "diagonal",
                          :sourceY {:expr "scale('y', datum.yc)"},
                          :sourceX {:expr "scale('x', 'stk1') + bandwidth('x')"},
                          :targetY {:expr "scale('y', datum.target.yc)"},
                          :targetX {:expr "scale('x', 'stk2')"}}
                         {:type "formula", :expr "range('y')[0]-scale('y', datum.size)", :as "strokeWidth"}
                         {:type "formula", :expr "datum.size/domain('y')[1]", :as "percentage"}]}],
     :scales [{:name "x", :type "band", :range "width", :domain ["stk1" "stk2"], :paddingOuter 0.05, :paddingInner 0.95}
              {:name "y", :type "linear", :range "height", :domain {:data "nodes", :field "y1"}}
              {:name "color", :type "ordinal", :range "category", :domain {:data "rawData", :fields ["stk1" "stk2"]}}
              {:name "stackNames", :type "ordinal", :range ["Source" "Destination"], :domain ["stk1" "stk2"]}],
     :axes [{:orient "bottom", :scale "x", :encode {:labels {:update {:text {:scale "stackNames", :field "value"}}}}}
            {:orient "left", :scale "y"}],
     :marks [{:type "rect",
              :from {:data "nodes"},
              :encode {:enter {:stroke {:value "#000"},
                               :strokeWidth {:value 2},
                               :width {:scale "x", :band 1},
                               :x {:scale "x", :field "stack"},
                               :y {:field "y0", :scale "y"},
                               :y2 {:field "y1", :scale "y"}}}}
             {:type "path",
              :name "edgeMark",
              :from {:data "edges"},
              :clip true,
              :encode {:update {:stroke [{:test "groupSelector && groupSelector.stack=='stk1'",
                                          :scale "color",
                                          :field "stk2"}
                                         {:scale "color", :field "stk1"}],
                                :strokeWidth {:field "strokeWidth"},
                                :path {:field "path"},
                                :strokeOpacity {:signal "!groupSelector && (groupHover.stk1 == datum.stk1 || groupHover.stk2 == datum.stk2) ? 0.9 : 0.3"},
                                :zindex {:signal "!groupSelector && (groupHover.stk1 == datum.stk1 || groupHover.stk2 == datum.stk2) ? 1 : 0"},
                                :tooltip {:signal "datum.stk1 + ' ? ' + datum.stk2 + '    ' + format(datum.size, ',.0f') + '   (' + format(datum.percentage, '.1%') + ')'"}},
                       :hover {:strokeOpacity {:value 1}}}}
             {:type "rect",
              :name "groupMark",
              :from {:data "groups"},
              :encode {:enter {:fill {:scale "color", :field "grpId"}, :width {:scale "x", :band 1}},
                       :update {:x {:scale "x", :field "stack"},
                                :y {:field "scaledY0"},
                                :y2 {:field "scaledY1"},
                                :fillOpacity {:value 0.6},
                                :tooltip {:signal "datum.grpId + '   ' + format(datum.total, ',.0f') + '   (' + format(datum.percentage, '.1%') + ')'"}},
                       :hover {:fillOpacity {:value 1}}}}
             {:type "text",
              :from {:data "groups"},
              :interactive false,
              :encode {:update {:x {:signal "scale('x', datum.stack) + (datum.rightLabel ? bandwidth('x') + 8 : -8)"},
                                :yc {:signal "(datum.scaledY0 + datum.scaledY1)/2"},
                                :align {:signal "datum.rightLabel ? 'left' : 'right'"},
                                :baseline {:value "middle"},
                                :fontWeight {:value "bold"},
                                :text {:signal "abs(datum.scaledY0-datum.scaledY1) > 13 ? datum.grpId : ''"}}}}
             {:type "group",
              :data [{:name "dataForShowAll", :values [{}], :transform [{:type "filter", :expr "groupSelector"}]}],
              :encode {:enter {:xc {:signal "width/2"}, :y {:value 30}, :width {:value 80}, :height {:value 30}}},
              :marks [{:type "group",
                       :name "groupReset",
                       :from {:data "dataForShowAll"},
                       :encode {:enter {:cornerRadius {:value 6},
                                        :fill {:value "#f5f5f5"},
                                        :stroke {:value "#c1c1c1"},
                                        :strokeWidth {:value 2},
                                        :height {:field {:group "height"}},
                                        :width {:field {:group "width"}}},
                                :update {:opacity {:value 1}},
                                :hover {:opacity {:value 0.7}}},
                       :marks [{:type "text",
                                :interactive false,
                                :encode {:enter {:xc {:field {:group "width"}, :mult 0.5},
                                                 :yc {:field {:group "height"}, :mult 0.5, :offset 2},
                                                 :align {:value "center"},
                                                 :baseline {:value "middle"},
                                                 :fontWeight {:value "bold"},
                                                 :text {:value "Show All"}}}}]}]}],
     :signals [{:name "groupHover",
                :value {},
                :on [{:events "@groupMark:mouseover",
                      :update "{stk1:datum.stack=='stk1' && datum.grpId, stk2:datum.stack=='stk2' && datum.grpId}"}
                     {:events "mouseout", :update "{}"}]}
               {:name "groupSelector",
                :value false,
                :on [{:events "@groupMark:click!",
                      :update "{stack:datum.stack, stk1:datum.stack=='stk1' && datum.grpId, stk2:datum.stack=='stk2' && datum.grpId}"}
                     {:events [{:type "click", :markname "groupReset"} {:type "dblclick"}], :update "false"}]}]})


(defn draw-sankey []
  (oz/vega sankey-data))
