(ns widget-workshop.util.vectors)



(defn splice [coll at d & n]
  (let [[a b] (split-at at coll)
        c (drop d b)
        x (if n (concat a n c) (concat a c))]
    (into [] x)))



(defn disjoin [v item]
  (splice v (.indexOf v item) 1))


;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; removing an entry from a vector, like a 'disj'
;
(comment

  (def v ["1" "2" "3"])
  (splice v (.indexOf v "2") 1)

  (disjoin v "2")


  ())
