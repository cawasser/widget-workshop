(ns widget-workshop.server.source.util)


(defn keyset
  " returns a collection of all keys found across all the 'entitis'
  in the given collection

  Assumptions

  - expects 'coll' to be a collection (seq/vector) of maps, one per entity

  returns seq of keywords, one for each key used across all the entities"

  [coll]

  (->> coll
    (map keys)
    (apply clojure.set/union)
    (into #{})
    sort))