(ns widget-workshop.views.home-page
  (:require [reagent.core :as r]))


(defonce empty-eq {:x 1 :y 1 :op "+" :total 2})

(defonce answers* (r/atom [{:x 5 :y 18 :op "+" :total 0}
                           {:x 9 :y 3 :op "+" :total 0}
                           {:x 2 :y 19 :op "+" :total 0}]))

(defn- new-equation []
  (swap! answers* conj empty-eq))


(defn- set-key* [idx k new-val]
  (swap! answers* assoc-in [idx k] new-val))



(defn get-answer* [idx]
  (let [data (get @answers* idx)
        x    (:x data) y (:y data) op (:op data)
        path (str "/api/math/"
               (condp = op
                 "+" "plus"
                 "-" "minus"
                 "*" "mult"
                 "/" "div"))]
    (prn "post " path)))
    ;(POST path
    ;  {:headers {"Accept" "application/transit+json"}
    ;   :params  {:x x :y y}
    ;   :handler #(swap! answers* assoc-in [idx :total] (:total %))})))



(defn input-field [tag id idx data]
  [:div.field
   [tag
    {:type        :number
     :value       (id data)
     :placeholder (name id)
     :on-change   #(do
                     (prn "clicked " id idx)
                     (swap! answers* assoc-in [idx id] (js/parseInt (-> % .-target .-value)))
                     (get-answer* idx))}]])


(defn colored-field [tag data]
  [tag {:class (cond
                 (< data 0) "negative-result"
                 (and (<= 0 data) (< data 20)) "small-result"
                 (and (<= 20 data) (< data 50)) "medium-result"
                 (<= 50 data) "large-result")}
   (str data)])



(defn- make-row [idx data]
  ^{:key idx}
  [:tr
   [:td (str idx)]
   [:td [input-field :input.input :x idx data]]
   [:td [:select {:style     {:font-size :xx-large}
                  :on-change #(do
                                (prn "clicked :op " idx)
                                (set-key* idx :op (-> % .-target .-value))
                                (get-answer* idx))}
         (map #(into ^{:key %} [:option %]) ["+" "-" "*" "/"])]]
   [:td [input-field :input.input :y idx data]]
   [:td "="]
   (colored-field :td.result (:total data))])



(defn about-page []
  [:section.section>div.container>div.content
   [:h1 "something new"]
   [:img {:src "/img/warning_clojure.png"}]])


(defn home-page []
  [:section.section>div.container>div.content
   [:div.button.is-medium.is-danger {:on-click #(new-equation)}
    [:i.fas.fa-plus]]
   [:table
    [:tbody
     (doall
       (for [idx (range (count @answers*))]
         (let [data (get @answers* idx)]
           ;(prn data)
           (make-row idx data))))]]])