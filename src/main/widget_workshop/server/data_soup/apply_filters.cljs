(ns widget-workshop.server.data-soup.apply-filters)


(defmulti filter-step
  (fn [[step params] data] step))


; 'ds'stands for 'data soup'

;     [:ds/select (nee query/filter) [<fn> OR? <keywords>]
;          - how sophisticated do we make this?
;
;     [:ds/map <fn>]
;     [:ds/take <int>]
;     [:ds/reduce <fn>]
;     [:ds/first]
;     [:ds/last]
;     [:ds/nth n]
;     [:ds/extract (nee 'map juxt') [<keyword>]]
;     [:ds/pipe (nee 'map comp') [<fn>]]
;     [:ds/group-by [<keyword>]]




(defmethod filter-step :ds/map [[step {:keys [params value]}] data]
  (map (apply juxt value) data))


(defmethod filter-step :ds/take [[step {:keys [params value]}] data]
  (take value data))


(defmethod filter-step :ds/first [[step {:keys [params value]}] data]
  (first data))


(defmethod filter-step :ds/last [[step {:keys [params value]}] data]
  (last data))


(defmethod filter-step :ds/extract [[step {:keys [params value]}] data]
  (->> data
    (map (apply juxt value))
    (map #(zipmap value %))))


(defmethod filter-step :ds/pipe [[step {:keys [params value]}] data]
  (->> data
    (map (apply comp value))))


(defmethod filter-step :ds/group-by [[step {:keys [params value]}] data]
  (->> data
    (group-by (apply juxt value))))






(defn apply-filters [dsl data]
  (if (empty? dsl)
    data
    (recur (rest dsl) (filter-step (first dsl) data))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; so now we can develop our multi-methods for all 9 cases (currently)
;
(comment

  (def pipeline2 [[:ds/extract {:param {:vector :keyword}
                                :value [:datetime :id :param-2]}]
                  [:ds/group-by {:param {:vector :keyword}
                                 :value [:id]}]
                  [:ds/take {:param {:scalar :number}
                             :value 2}]])

  (filter-step (first pipeline2)
    (:data (widget-workshop.server.source.config-data/get-data)))

  (->> (:data (widget-workshop.server.source.config-data/get-data))
    (filter-step (nth pipeline2 0))
    (filter-step (nth pipeline2 1))
    (filter-step (nth pipeline2 2)))


  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; so now we can develop our multi-methods for all 9 (10?) cases (currently)
;
(comment
  (apply-filters [[:ds/extract [:datetime :id :param-2]]
                  [:ds/group-by [:id]]]
    (:data (widget-workshop.server.source.config-data/get-data)))

  (apply-filters []
    (:data (widget-workshop.server.source.config-data/get-data)))

  ())