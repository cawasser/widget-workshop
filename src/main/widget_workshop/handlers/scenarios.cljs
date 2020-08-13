(ns widget-workshop.handlers.scenarios)



(defn scenario?
  "examines the drag and drop scenario and returns a keyword
  that describes the user's intent"

  [db from to]

  (cond
    (= from to "data-sources-list") :do-nothing

    ; reorder the 'filters' on a widget
    (and
      (not= from "data-sources-list")
      (not= to (:blank-widget db))
      (= from to)) :reorder-filters

    ; dropping from the sources onto a new widget
    (and
      (= from "data-sources-list")
      (= to (:blank-widget db))) :new-widget-from-source

    ; drop from an existing widget onto the 'new' widget
    (and
      (not= from "data-sources-list")
      (= to (:blank-widget db))) :new-widget-from-widget

    ; drop from one widget to another
    (and
      (not= from "data-sources-list")
      (not= to (:blank-widget db))) :connect-widgets

    ; drop new sources onto a widget (not a new widget)
    (and
      (= from "data-sources-list")
      (not= to (:blank-widget db))) :add-source-to-widget

    ; can't do anything else
    :default :do-nothing))



