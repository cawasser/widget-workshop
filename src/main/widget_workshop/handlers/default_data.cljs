(ns widget-workshop.handlers.default-data
  (:require [widget-workshop.server.source.generic-data]
            [widget-workshop.server.source.config-data]))


(def sample-generic-data
  [{:id 1 :x 100 :y 100 :datetime #inst"2020-08-11T10:00:00.000Z"}
   {:id 2 :x 200 :y 50 :datetime #inst"2020-08-11T11:00:00.000Z"}
   {:id 3 :x 300 :y 25 :datetime #inst"2020-08-11T12:00:00.000Z"}
   {:id 4 :x 400 :y 12.5 :datetime #inst"2020-08-11T13:00:00.000Z"}])

(def sample-config-data
  [{:datetime #inst"2020-08-11T10:00:00.000Z" :id 1 :kind "alpha" :param-1 "off" :param-2 "off"}
   {:datetime #inst"2020-08-11T11:00:00.000Z" :id 1 :kind "alpha" :param-1 "off" :param-2 "on"}
   {:datetime #inst"2020-08-11T12:00:00.000Z" :id 1 :kind "alpha" :param-1 "on" :param-2 "off"}
   {:datetime #inst"2020-08-11T13:00:00.000Z" :id 1 :kind "alpha" :param-1 "on" :param-2 "on"}
   {:datetime #inst"2020-08-11T13:00:00.000Z" :id 1 :kind "alpha" :param-1 "off" :param-2 "off"}

   {:datetime #inst"2020-08-11T10:00:00.000Z" :id 2 :kind "alpha" :param-1 "on" :param-2 "on"}
   {:datetime #inst"2020-08-11T12:00:00.000Z" :id 2 :kind "alpha" :param-1 "on" :param-2 "off"}
   {:datetime #inst"2020-08-11T13:00:00.000Z" :id 2 :kind "alpha" :param-1 "on" :param-2 "on"}

   {:datetime #inst"2020-08-11T12:00:00.000Z" :id 3 :kind "alpha" :param-1 "of" :param-2 "on"}
   {:datetime #inst"2020-08-11T15:00:00.000Z" :id 3 :kind "alpha" :param-1 "on" :param-2 "off"}
   {:datetime #inst"2020-08-11T17:00:00.000Z" :id 3 :kind "alpha" :param-1 "on" :param-2 "on"}

   ; for example, :id 4 has different params than the other 3:
   {:datetime #inst"2020-08-11T12:00:00.000Z" :id 4 :kind "alpha 2" :param-1 "of" :param-3 "on"}
   {:datetime #inst"2020-08-11T15:00:00.000Z" :id 4 :kind "alpha 2" :param-1 "on" :param-3 "off"}
   {:datetime #inst"2020-08-11T17:00:00.000Z" :id 4 :kind "alpha 2" :param-1 "on" :param-3 "on"}

   ; for example, :id 5 is a completely different 'thing':
   {:datetime #inst"2020-08-11T12:00:00.000Z" :id 5 :kind "beta" :x 100 :y 100}
   {:datetime #inst"2020-08-11T15:00:00.000Z" :id 5 :kind "beta" :x 120 :y 100}
   {:datetime #inst"2020-08-11T17:00:00.000Z" :id 5 :kind "beta" :x 140 :y 100}])

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
   :builder/sources        {"generic-source" {:name "generic-source" :sample sample-generic-data}
                            "config-source"  {:name "config-source" :sample sample-config-data}}

   ;; a map of :steps to the dsl used to actually perform the operation
   ;; on a data set
   ;:builder/step-source
   ;                        {}

   ; a vector of :filter ids
   ;
   ; used to create the UI for dragging :steps
   :builder/steps-list     ["extract" "group-by" "first" "last" "take" "drop"]

   ;; map of UUIDs to uniquely identify a draggable item, each mapped to {:id} which
   ;; provides human-readable naming for the item
   ;;
   ;; used to create each draggable in the UI (sidebar or widgets)
   ;:builder/steps          {"alpha" ["2239ee68-bfef-4074-92e3-0809ca0e593e"
   ;                                  "7bc854f6-b351-4bf0-b097-43b05e501f4a"]}
   ;
   ; uuids for widgets 'under construction' on the builder page
   ;
   ; used to generate the widgets in the gallery  UI
   :builder/widget-list    ["alpha" "beta" "delta"]

   ; map of uuids to {:id <uuid> :name <name>} for each draggble, so they are uniquely identified
   ; throughout the entire app
   ;
   ; use to identify draggable items for use in the UI, as well as any data needed for
   ; UI presentation (id, color, etc.)
   ;     [:ds/first]
   ;     [:ds/last]
   ;     [:ds/nth n]

   :builder/drag-items     {
                            "generic-source" {:id "generic-source" :type :source :name "generic-source"}
                            "config-source"  {:id "config-source" :type :source :name "config-source"}
                            "group-by"       {:id     "group-by" :type :step :name "group-by"
                                              :static true
                                              :step   [:group-by {:param {:vector :keyword}
                                                                  :value []}]}
                            "first"          {:id     "first" :type :step :name "first"
                                              :static true :step [:first {:param :none}]}
                            "last"           {:id     "last" :type :step :name "last"
                                              :static true :step [:last {:param :none}]}
                            "take"           {:id     "take" :type :step :name "take"
                                              :static true
                                              :step   [:take {:param {:scalar :number}
                                                              :value 5}]}
                            "drop"           {:id     "drop" :type :step :name "drop"
                                              :static true
                                              :step   [:take {:param {:scalar :number}
                                                              :value 5}]}
                            "extract"        {:id     "extract" :type :step :name "extract"
                                              :static true
                                              :step   [:extract {:param {:vector :keyword}
                                                                 :value []}]}}

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
   :widgets                {"alpha" {:id     "alpha" :name "Alpha"
                                     :source ""
                                     :steps  []}
                            "beta"  {:id     "beta" :name "Beta"
                                     :source ""
                                     :steps  []}
                            "delta" {:id     "delta" :name "Delta"
                                     :source ""
                                     :steps  []}}})



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