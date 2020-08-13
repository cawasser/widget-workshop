(ns widget-workshop.util.vectors)



(defn splice
  "mimics the JS 'splice' function

  can be used to remove 'd' items and add 'n' items for the given coll(ection) 'at'
  that given location

  eg.

  (def v [1 2 3 4]) => 'v'
  (splice v 2 1 :a) => [1 2 :a 4]

  "

  [coll at d & n]
  (let [[a b] (split-at at coll)
        c (drop d b)
        x (if n (concat a n c) (concat a c))]
    (into [] x)))


(defn reorder [coll from to]
  ;(prn "reorder " coll from to)
  (let [item (nth coll from)
        a (splice coll from 1)
        b (splice a to 0 item)]
    b))



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


  (def v [1 2 3 4])
  (splice v 2 1 :a)

  ())


;;;;;;;;;;;;;;;;;;;;;;;;;;
;
; swap 2 entries in avector
;
(comment

  (def v ["1" "2" "3"])
  (def from 1)
  (def to 0)


  (let [item (nth v 1)
        a (splice v from 1)
        b (splice a to 0 item)]
    b)

  (reorder v 1 0)
  (reorder v 2 1)
  (reorder v 0 1)
  (reorder v 0 2)

  ())
