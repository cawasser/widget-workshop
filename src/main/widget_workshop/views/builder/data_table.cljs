(ns widget-workshop.views.builder.data-table
  (:require [reagent.core :as r :refer [atom]]
            [reagent-table.core :as rt]
            [re-frame.core :as rf]
            [goog.i18n.NumberFormat.Format])

  (:import
    (goog.i18n NumberFormat)
    (goog.i18n.NumberFormat Format)))



(rf/reg-event-db
  :toggle-link
  (fn [db [_ widget-id link-id]]
    (let [orig (get-in db [:widgets widget-id :links])]
      (assoc-in db [:widgets widget-id :links]
        (if (contains? orig link-id)
          (disj orig link-id)
          (conj orig link-id))))))


(rf/reg-sub
  :links
  (fn [db [_ widget-id]]
    (get-in db [:widgets widget-id :links])))


(enable-console-print!)

(def nff (NumberFormat. Format/DECIMAL))

(defn format-number
  [num]
  (.format nff (str num)))



(defn- cell-data
  "Resolve the data within a row for a specific column"
  [row cell]
  (let [{:keys [path expr]} cell]
    (or (and path
          (get-in row path))
      (and expr
       (expr row)))))



(defn- cell-fn
  "Return the cell hiccup form for rendering.
   - render-info the specific column from :column-model
   - row the current row
   - row-num the row number
   - col-num the column number in model coordinates"
  [render-info row row-num col-num]
  (let [{:keys [format attrs]
         :or   {format identity
                attrs (fn [_] {})}} render-info
        data    (cell-data row render-info)
        content (format data)
        attrs   (attrs data)]
    [:span attrs content]))



(defn date?
  "Returns true if the argument is a date, false otherwise."
  [d]
  (instance? js/Date d))



(defn date-as-sortable
  "Returns something that can be used to order dates."
  [d]
  (.getTime d))



(defn compare-vals
  "A comparator that works for the various types found in table structures.
  This is a limited implementation that expects the arguments to be of
  the same type. The :else case is to call compare, which will throw
  if the arguments are not comparable to each other or give undefined
  results otherwise.

  Both arguments can be a vector, in which case they must be of equal
  length and each element is compared in turn."
  [x y]
  (cond
    (and (vector? x)
      (vector? y)
      (= (count x) (count y)))
    (reduce #(let [r (compare (first %2) (second %2))]
               (if (not= r 0)
                 (reduced r)
                 r))
      0
      (map vector x y))

    (or (and (number? x) (number? y))
      (and (string? x) (string? y))
      (and (boolean? x) (boolean? y)))
    (compare x y)

    (and (date? x) (date? y))
    (compare (date-as-sortable x) (date-as-sortable y))

    :else ;; hope for the best... are there any other possiblities?
    (compare x y)))

(defn- sort-fn
  "Generic sort function for tabular data. Sort rows using data resolved from
  the specified columns in the column model."
  [rows column-model sorting]
  (sort (fn [row-x row-y]
          (reduce
            (fn [_ sort]
              (let [column (column-model (first sort))
                    direction (second sort)
                    cell-x (cell-data row-x column)
                    cell-y (cell-data row-y column)
                    compared (if (= direction :asc)
                               (compare-vals cell-x cell-y)
                               (compare-vals cell-y cell-x))]
                (when-not (zero? compared)
                  (reduced compared))))

            0
            sorting))
    rows))

(def table-state (atom {:draggable true}))


(defn data-table [table-data columns row-key]
  (let [table-state (atom {:draggable true})]
    [:div.container {:style {:font-size 16 :margin-top 10} :height "100%"}
     ;[:div.panel.panel-default
     ;[:div.panel-body
     [rt/reagent-table table-data {:table {:class "table table-hover table-striped table-bordered table-transition"
                                           :style {:border-spacing 0
                                                   :border-collapse "separate"}
                                           :on-double-click #(prn "DOUBLE CLICK!" (-> % .-target .-textContent))}
                                   :table-container {:style {:border "1px solid green"}}
                                   :th {:style {:border "1px solid white" :background-color "mediumblue" :color "white"}}
                                   :table-state  table-state
                                   :scroll-height "20vh"
                                   :column-model columns
                                   :row-key      #(fn [row _] (do
                                                                ;(prn "row-key" (get-in row row-key))
                                                                (get-in row row-key)))
                                   :render-cell  cell-fn
                                   :sort         sort-fn}]]))
                                 ;:caption [:caption "Test caption"]
                                 ;:column-selection {:ul
                                 ;                   {:li {:class "btn"}}}}]])


