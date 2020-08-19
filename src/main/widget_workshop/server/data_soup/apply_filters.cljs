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
;     [:ds/chain (nee 'map comp') [<fn>]]
;     [:ds/group-by [<keyword>]]




(defmethod filter-step :map [[step params] data]
  (map (apply juxt params) data))


(defmethod filter-step :take [[step params] data]
  (take params data))


(defmethod filter-step :first [[step params] data]
  (first data))


(defmethod filter-step :last [[step params] data]
  (last data))


(defmethod filter-step :extract [[step params] data]
  (->> data
    (map (apply juxt params))
    (map #(zipmap params %))))


(defmethod filter-step :chain [[step params] data]
  (->> data
    (map (apply comp params))))


(defmethod filter-step :group-by [[step params] data]
  (->> data
    (group-by (apply juxt params))))






(defn apply-filters [dsl data]
  (if (empty? dsl)
    data
    (recur (rest dsl) (filter-step (first dsl) data))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; so now we can develop our multi-methods for all 9 cases (currently)
;
(comment

  (def pipeline2 [[:extract [:datetime :id :param-2]]
                  [:group-by [:id]]
                  [:take 2]])

  (filter-step (first pipeline2)
    (:data (widget-workshop.server.source.config-data/get-data)))

  (->> (:data (widget-workshop.server.source.config-data/get-data))
    (filter-step (nth pipeline2 0))
    (filter-step (nth pipeline2 1))
    (filter-step (nth pipeline2 2)))


  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; so now we can develop our multi-methods for all 9 cases (currently)
;
(comment
  (apply-filters [[:extract [:datetime :id :param-2]]
                  [:group-by [:id]]]
    (:data (widget-workshop.server.source.config-data/get-data)))

  ())