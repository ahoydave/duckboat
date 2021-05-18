(ns duckwatch.temp-data
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [duckwatch.mutations]
            [duckwatch.client]
            [com.fulcrologic.fulcro.components :as comp]))

(defn extract-data [plot-data dev-id]
  (->> plot-data
       (filter #(= (:deviceID %) dev-id))
       (map (fn [{:keys [timestamp value]}]
              {:timestamp (js/Date. (* (js/parseInt timestamp) 1000))
               :value     (/ (js/parseInt value) 1000)}))
       (filter #(< (js/Date. (- (js/Date.now) (* 3 24 60 60 1000))) (:timestamp %)))
       (sort-by :timestamp)))

(comment
  (def mynum 1)

  (go (let [{:keys [status body]} (<! (http/get "https://r0k6trqwv9.execute-api.eu-west-2.amazonaws.com/prod/sensorreadings"
                                                {:with-credentials? false}))
            devid1                (first (sort (vec (set (map :deviceID body)))))
            devid2                (second (sort (vec (set (map :deviceID body)))))
            new-data-1            (extract-data body devid1)
            new-data-2            (extract-data body devid2)]
        (prn "Fetching temperature data status: " status)
        (comp/transact! duckwatch.client/app
                        [(duckwatch.mutations/set-plot-data
                           {:plot-data {:data1 new-data-1
                                        :data2 new-data-2}})])))

  (def devid1 (first (sort (vec (set (map :deviceID @raw))))))
  (def devid2 (second (sort (vec (set (map :deviceID @raw))))))

  (str (- (/ (js/Date.now) 1000) (* 24 60 60)))
  ;; => "1618646975.046"
  (js/Date. (* 1618212971 1000))


  (def new-data-1 (extract-data @raw devid1))
  (def new-data-2 (extract-data @raw devid2))
  (prn (count new-data-1))

  new-data-1
  new-data-2
  (comp/transact! duckwatch.client/app
                  [(duckwatch.mutations/set-plot-data
                     {:plot-data {:data1 new-data-1
                                  :data2 new-data-2}})])
  )
