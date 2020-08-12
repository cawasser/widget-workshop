(ns widget-workshop.util.uuid
  (:require [cljs-uuid-utils.core :as uuid]))



(defn aUUID [] (uuid/uuid-string (uuid/make-random-uuid)))