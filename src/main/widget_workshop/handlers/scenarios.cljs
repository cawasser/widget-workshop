(ns widget-workshop.handlers.scenarios)



(defn strip-suffix
  "strip any suffix (@source, @filter) from the given 'name'"

  [name]
  (.substr name 0 (.indexOf name "@")))



(def drop-scenario-values [:do-nothing                      ; default
                           :add-source-to-widget
                           :connect-widgets
                           :new-widget-from-source
                           :new-widget-from-widget
                           :reorder-steps])

(defn drop-scenario?
  "examines the drag and drop scenario and returns a keyword
  that describes the user's intent"

  [from to]

  ;(prn "drop-scenario?" from to)
  (cond
    ; can't drop into the data sources or :steps lists
    (= to "builder/sources-list") :do-nothing
    (= to "builder/steps-list") :do-nothing

    ; drop new sources onto an existing widget (not a new widget)
    (= from "builder/sources-list") :add-source-to-widget

    ; drop new filter onto an existing widget (not a new widget)
    (= from "builder/steps-list") :add-step-to-widget

    ; reorder the ':steps' on a widget
    (and
      (not= from "builder/sources-list")
      (not= from "builder/steps-list")
      (= from to)) :reorder-steps

    ; can't do anything else
    :default :do-nothing))



(comment
  (def from "something")
  (if (<= 0 (.indexOf from "@source")) true false)

  ())
