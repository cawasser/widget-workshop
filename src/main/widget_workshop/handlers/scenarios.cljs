(ns widget-workshop.handlers.scenarios
  (:require [widget-workshop.views.dnd.new-widget :refer [new-widget-id]]))


(def drop-scenario-values [:do-nothing                      ; default
                           :add-source-to-widget
                           :connect-widgets
                           :new-widget-from-source
                           :new-widget-from-widget
                           :reorder-filters])

(defn drop-scenario?
  "examines the drag and drop scenario and returns a keyword
  that describes the user's intent"

  [from to]

  (cond
    ; can't do anything within the data sources or filters lists
    (= from to "builder/data-sources-list") :do-nothing
    (= from to "builder/filter-list") :do-nothing

    ; can't drop into the data sources or filters lists
    (= to "builder/data-sources-list") :do-nothing
    (= to "builder/filter-list") :do-nothing

    ; reorder the 'filters' on a widget
    (and
      (not= from "builder/data-sources-list")
      (not= to new-widget-id)
      (= from to)) :reorder-filters

    ; dropping from the sources onto a new widget
    (and
      (= from "builder/data-sources-list")
      (= to new-widget-id)) :new-widget-from-source

    ; dropping from the filters onto a new widget
    ;(and
    ;  (= from "filter-list")
    ;  (= to new-widget-id)) :new-widget-from-filters

    ; drop from an existing widget onto the 'new' widget
    ;(and
    ;  (not= from "data-sources-list")
    ;  (not= from "filter-list")
    ;  (= to new-widget-id)) :new-widget-from-widget

    ; drop from an existing widget onto the 'new' widget
    ;(and
    ;  (not= from "data-sources-list")
    ;  (not= from "filter-list")
    ;  (= to new-widget-id)) :new-widget-from-widget

    ; drop from one widget to another
    ;(and
    ;  (not= from "data-sources-list")
    ;  (not= from "filter-list")
    ;  (not= to new-widget-id)) :connect-widgets

    ; drop new sources onto an exsiting widget (not a new widget)
    (and
      (= from "builder/data-sources-list")
      (not= to new-widget-id)) :add-source-to-widget

    ; drop new filter onto an existing widget (not a new widget)
    (and
      (= from "builder/filter-list")
      (not= to new-widget-id)) :add-filter-to-widget

    ; can't do anything else
    :default :do-nothing))



