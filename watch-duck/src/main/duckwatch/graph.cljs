(ns duckwatch.graph
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
            ["victory" :refer [VictoryChart VictoryAxis VictoryLine VictoryLabel VictoryArea]]))

(def vchart (interop/react-factory VictoryChart))
(def vaxis (interop/react-factory VictoryAxis))
(def vline (interop/react-factory VictoryLine))
(def vlabel (interop/react-factory VictoryLabel))
(def varea (interop/react-factory VictoryArea))

(defn moving-average [data window-size]
  (let [padded-start (concat (repeat (/ window-size 2) (first data))
                             data
                             (repeat (/ (+ window-size 0.5) 2) (first data)))
        windows (partition window-size 1 (repeat (last data)) padded-start)
        averages (map #(/ (reduce + %) (count %)) windows)]
    averages))

(comment
  (def state (com.fulcrologic.fulcro.application/current-state duckwatch.client/app))
  (def plot-data
    (com.fulcrologic.fulcro.algorithms.denormalize/db->tree [:plot-data] state state))
  (def data1 (-> plot-data :plot-data :data1))
  (def ave1 (mapv (fn [orig ave]
                   (assoc orig :value ave))
                 data1
                 (moving-average (map :value data1) 10)))
  )

(defsc YearlyValueChart [this {:keys [label plot-data x-step]}]
  (let [averaging 300
        data1   (:data1 plot-data)
        data2   (:data2 plot-data)
        data3   (:data3 plot-data)
        points1 (mapv (fn [{:keys [timestamp value]}]
                        {:x timestamp
                         :y value})
                      data1)
        points2 (mapv (fn [{:keys [timestamp value]}]
                        {:x timestamp
                         :y value})
                      data2)
        points3 (mapv (fn [{:keys [timestamp value]}]
                        {:x timestamp
                         :y value})
                      data3)
        ave1 (mapv (fn [{:keys [timestamp]} ave]
                     {:x timestamp
                      :y ave})
                   data1
                   (moving-average (map :value data1) averaging))
        ave2 (mapv (fn [{:keys [timestamp]} ave]
                     {:x timestamp
                      :y ave})
                   data2
                   (moving-average (map :value data2) averaging))
        times (mapv (fn [{:keys [timestamp value]}]
                      timestamp)
                      data1)]
    (tap> points3)
    ;; (tap> ave1)
    ;; (tap> ave2)
    (dom/div
      {}
      (vchart
        {}
        (varea {:style  {:data {:fill "#ffffff"}}
                :domain {:y [0 0.001]}
                :interpolation "step"
                :data   points3})
        (vline {:style  {:data {:stroke "black"}}
                :domain {:y [0 27]}
                :data   ave1})
        (vline {:style  {:data {:stroke "black"}}
                :domain {:y [0 27]}
                :data   ave2})
        (vline {:style  {:data {:stroke "blue"}}
                :domain {:y [0 27]}
                :data   points1})
        (vline {:style  {:data {:stroke "green"}}
                  :domain {:y [0 27]}
                  :data   points2})))))

(def yearly-value-chart (comp/factory YearlyValueChart))
