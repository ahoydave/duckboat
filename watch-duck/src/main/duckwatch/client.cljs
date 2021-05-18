(ns duckwatch.client
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [cljs-http.client :as cljs-http]
   [cljs.core.async :refer [<!]]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom]
   [com.fulcrologic.fulcro.networking.http-remote :as http]
   [com.fulcrologic.fulcro.data-fetch :as df]
   [duckwatch.ui :as ui]))

(defn get-request-middleware [req]
  (assoc req :method :get))

(defn extract-data [plot-data dev-id value-mult]
  (->> plot-data
       (filter #(= (:deviceID %) dev-id))
       (map (fn [{:keys [timestamp value]}]
              {:timestamp (js/Date. (* (js/parseInt timestamp) 1000))
               :value     (* (js/parseInt value) value-mult)}))
       (filter #(< (js/Date. (- 1620025148013  (* 7 24 60 60 1000))) (:timestamp %)))
       (sort-by :timestamp)))

(def resp-atom (atom nil))

(comment
  (def body-data (:body @resp-atom))
  (extract-data body-data "camera_rek_1")
  (sort (vec (set (map :deviceID body-data)))))

(defonce app
  (app/fulcro-app
    {:remotes
     {#_#_:remote (http/fulcro-http-remote {})}}))

(defn extract-plot-data [{:keys [body]}]
  (let [devid1     "/sys/bus/w1/devices/28-0119354dd248/w1_slave"
        devid2     "/sys/bus/w1/devices/28-012022e1b3c2/w1_slave"
        devid3 "camera_rek_1"
        new-data-1 (extract-data body devid1 0.001)
        new-data-2 (extract-data body devid2 0.001)
        new-data-3 (extract-data body devid3 27)
        result     {:data1 new-data-1
                    :data2 new-data-2
                    :data3 new-data-3}]
   result))

(defn fetch-graph-data []
  (go (let [response (<! (cljs-http/get "https://r0k6trqwv9.execute-api.eu-west-2.amazonaws.com/prod/sensorreadings"
                                        {:with-credentials? false}))]
        ;; (tap> response)
        ;; (reset! resp-atom response)
        (prn "sensor response" (:status response))
        (swap! (::app/state-atom app) assoc :plot-data (-> response
                                                           extract-plot-data))
        (app/schedule-render! app))))

(defn ^:export init
  "Shadow-cljs sets this up to be our entry-point function. See shadow-cljs.edn `:init-fn` in the modules of the main build."
  []
  (app/mount! app ui/Root "app")
  #_(df/load! app :friends ui/PersonList)
  #_(df/load! app :enemies ui/PersonList)
  (fetch-graph-data)
  ;; (js/setInterval fetch-graph-data 600000)

  (js/console.log "Loaded"))

(defn ^:export refresh
  "During development, shadow-cljs will call this on every hot reload of source. See shadow-cljs.edn"
  []
  ;; re-mounting will cause forced UI refresh, update internals, etc.
  (app/mount! app ui/Root "app")
  ;; As of Fulcro 3.3.0, this addition will help with stale queries when using dynamic routing:
  (comp/refresh-dynamic-queries! app)
  (js/console.log "Hot reload"))

(comment
  (comp/transact! app '[(duckwatch.mutations/fetch-plot-data {})]))
