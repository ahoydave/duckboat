(ns duckwatch.mutations
  (:require [clojure.pprint :refer [pprint]]
            [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [com.fulcrologic.fulcro.algorithms.merge :as merge]
            [com.fulcrologic.fulcro.application :as app]))

(defmutation delete-person
  "Delete a person"
  [{:keys [list-id person-id]}]
  (action [{:keys [state]}]
          (swap! state merge/remove-ident*
                 [:person/id person-id]
                 [:list/id list-id :list/people])))

(defn extract-data [plot-data dev-id]
  (->> plot-data
       (filter #(= (:deviceID %) dev-id))
       (map (fn [{:keys [timestamp value]}]
              {:timestamp (js/Date. (* (js/parseInt timestamp) 1000))
               :value     (/ (js/parseInt value) 1000)}))
       (filter #(< (js/Date. (- (js/Date.now) (* 3 24 60 60 1000))) (:timestamp %)))
       (sort-by :timestamp)))

(defmutation fetch-plot-data
  [params]
  #_(action [{:keys [state]}]
          (prn "fetching the plot data")
          (go (let [{:keys [status body]} (<! (http/get "https://r0k6trqwv9.execute-api.eu-west-2.amazonaws.com/prod/sensorreadings"
                                                        {:with-credentials? false}))
                    devid1                (first (sort (vec (set (map :deviceID body)))))
                    devid2                (second (sort (vec (set (map :deviceID body)))))
                    new-data-1            (extract-data body devid1)
                    new-data-2            (extract-data body devid2)]
                (prn "Fetch temperature data status: " status)
                (swap! state assoc :plot-data {:data1 new-data-1
                                               :data2 new-data-2}))))
  (sensor-remote [env] true)
  #_(result-action [env]
                 (prn "hello from inside the results fn")
                 (tap> env))
  (ok-action [env]
             (tap> env)))
