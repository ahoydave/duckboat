(ns duckwatch.server
  (:require
    [duckwatch.parser :refer [api-parser]]
    [org.httpkit.server :as http]
    [clojure.pprint :as pprint]
    [com.fulcrologic.fulcro.server.api-middleware :as server]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.resource :refer [wrap-resource]]))

(def ^:private not-found-handler
  (fn [req]
    {:status  404
     :headers {"Content-Type" "text/plain"}
     :body    "Dave says Not Found"}))

(defn wrap-logging [handler]
  (fn [req]
    (pprint/pprint (:body req))))

(def middleware
  (-> not-found-handler                                     ; (1)
    (server/wrap-api {:uri    "/api"
                      :parser api-parser})                  ; (2)
    wrap-logging
    (server/wrap-transit-params)
    (server/wrap-transit-response)
    (wrap-resource "public")                                ; (3)
    wrap-content-type))

(defonce stop-fn (atom nil))

(defn start []
  (reset! stop-fn (http/run-server middleware {:port 3000})))

(defn stop []
  (when @stop-fn
    (@stop-fn)
    (reset! stop-fn nil)))
