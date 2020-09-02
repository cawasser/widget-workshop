(ns widget-workshop.handlers.default-data
  (:require [widget-workshop.server.source.generic-data]
            [widget-workshop.server.source.config-data]))


(def init-db
  {
   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ; BUILDER DATA
   ;
   ; :builder/* keys are used to support the 'builder' features, where
   ; authorized users can create new widgets by defining data-sources
   ; and :steps

   ; list of :data-sources id's. should always contain the same uuids as
   ; :server/data-sources
   ;
   ; used to create the UI for dragging
   :builder/sources-list
                           ["generic-source" "config-source"]

   ; a map to a set with a single data-source for this widget, this is where the data
   ; will come from (singleton per widget)
   :builder/sources
                           {"generic-source" ["generic-source"]
                            "config-source"  ["config-source"]}

   ; a map of :steps to the dsl used to actually perform the operation
   ; on a data set
   :builder/step-source
                           {}

   ; a vector of :filter ids
   ;
   ; used to create the UI for dragging :steps
   :builder/steps-list      ["take" "extract" "f-1" "f-2"]

   ; map of UUIDs to uniquely identify a draggable item, each mapped to {:id} which
   ; provides human-readable naming for the item
   ;
   ; used to create each draggable in the UI (sidebar or widgets)
   :builder/steps          {"alpha" ["2239ee68-bfef-4074-92e3-0809ca0e593e"
                                     "7bc854f6-b351-4bf0-b097-43b05e501f4a"]}

   ; uuids for widgets 'under construction' on the builder page
   ;
   ; used to generate the widgets in the gallery  UI
   :builder/widget-list    ["alpha" "beta" "delta"]

   ; map of uuids to {:id <uuid> :name <name>} for each draggble, so they are uniquely identified
   ; throughout the entire app
   ;
   ; use to identify draggable items for use in the UI, as well as any data needed for
   ; UI presentation (id, color, etc.)
   :builder/drag-items     {
                            "generic-source"                       {:id "generic-source" :type :source :name "generic-source"}
                            "config-source"                        {:id "config-source" :type :source :name "config-source"}
                            "f-1"                                  {:id "f-1" :type :filter :name "f-1"}
                            "f-2"                                  {:id "f-2" :type :filter :name "f-2"}
                            "take"                                 {:id     "take" :type :filter :name "take"
                                                                    :filter [:take {:param {:scalar :number}}]}
                            "extract"                              {:id     "extract" :type :filter :name "extract"
                                                                    :filter [:extract {:param {:vector :keyword}}]}}
                            ;"23329dbf-ae47-410e-82e1-70487224fe36"
                            ;                                       {:id   "23329dbf-ae47-410e-82e1-70487224fe36"
                            ;                                        :type :source :name "generic-source"}
                            ;"b7170586-fe9d-4cc9-a84b-904a720cc343" {:id   "b7170586-fe9d-4cc9-a84b-904a720cc343"
                            ;                                        :type :source :name "config-source"}
                            ;"2239ee68-bfef-4074-92e3-0809ca0e593e"
                            ;                                       {:id     "2239ee68-bfef-4074-92e3-0809ca0e593e"
                            ;                                        :type   :filter :name "take"
                            ;                                        :filter [:take {:param {:scalar :number}}]}
                            ;"7bc854f6-b351-4bf0-b097-43b05e501f4a"
                            ;                                       {:id     "7bc854f6-b351-4bf0-b097-43b05e501f4a"
                            ;                                        :type   :filter :name "extract"
                            ;                                        :filter [:extract {:param {:vector :keyword}}]}}



   ; hold the uuid for the widget currently 'under construction'
   :builder/current-widget "alpha"

   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ; LIVE DATA
   ;
   ; :live/* keys are used to support the 'dashboard' features, where
   ; all users can create dashboards from existing widgets created using the
   ; 'builder' functionality

   ; :live/data holds the results of the last subscription for each data-source
   ;
   :live/data              {}

   ; map of uuid to relevant for 'real' widgets
   ;
   ; used to generate the widgets in the widget panel UI
   :live/widgets           ["alpha" "beta" "delta"]

   ;  map of uuid to relevant 'active' widgets, the one we actually SHOW
   ;
   :live/active-widgets    {}


   ; uuids for 'real' widgets
   ;
   ; used to generate the widgets in the widget sidebar UI
   :live/widget-list       ["one" "two" "three"]

   ;
   ;
   :live/widget-layout     {}


   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ; SERVER DATA
   ;
   ; :server/* keys are used to support the 'server' features since this
   ; is a cljs-only experiment. these keys would be moved to a real CLJ
   ; server

   ; map of the data-sources, with id mapped to the function used to generate
   ; the most up-to-date data
   :server/data-sources    {"generic-source" #()            ;widget-workshop.server.source.generic-data/get-data
                            "config-source"  #()}           ; widget-workshop.server.source.config-data/get-data}


   ; map of 'data-source' to map of the subscribers (:client, :widget)
   ;
   ; used to update clients when sources 'change'
   :server/subscriptions   {}



   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   ; WIDGETS
   ;
   ; since widgets show up in multiple places, we use this unqualified key
   ; to hold a map of the widget uuid's to the context and content
   ;
   :widgets                {"alpha" {:id     "alpha" :name "Alpha"}
                                     ;:source "b7170586-fe9d-4cc9-a84b-904a720cc343"
                                     ;:steps  ["f-1" "f-2"]}
                            "beta"  {:id     "beta" :name "Beta"
                                     :source "b7170586-fe9d-4cc9-a84b-904a720cc343"
                                     :steps  ["f-1" "f-2"]}
                            "delta" {:id     "delta" :name "Delta"
                                     :source "b7170586-fe9d-4cc9-a84b-904a720cc343"
                                     :steps  ["f-1" "f-2"]}}})



(defn gen-widget [id]
  {:id          id :name "Widget"
   :title-color "darkgray"
   :text-color  "white"
   :source      #{}
   :steps       []
   :content     {}})


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; generate some uuid's for static use
;
(comment

  (re-frame.core/dispatch-sync [:initialize])
  (widget-workshop.util.uuid/aUUID)


  (gen-widget "skadsljfkdajf")

  ())