(ns duckwatch.ui
  (:require [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            [com.fulcrologic.fulcro.dom :as dom]
            [duckwatch.mutations :as mutations]
            [duckwatch.graph :as graph]))

(defsc Person [this {:person/keys [id name age]} {:keys [on-delete]}]
  {:query         [:person/id :person/name :person/age]
   :ident         (fn [] [:person/id id])}
  (dom/li
    (dom/h5 (str name " (age: " age ")"))
    (dom/button {:onClick #(on-delete id)} "X")))

;; The keyfn generates a react key for each element based on props. See React documentation on keys.
(def ui-person (comp/factory Person {:keyfn :person/id}))

(defsc PersonList [this {:list/keys [id label people]}]
  {:query [:list/id :list/label {:list/people (comp/get-query Person)}]
   :ident (fn [] [:list/id id])}
  (let [delete-person
        (fn [person-id]
          (comp/transact! this [(mutations/delete-person {:list-id   id
                                                          :person-id person-id})]))]
    (dom/div
      (dom/h4 label)
      (dom/ul
        (map (fn [p] (ui-person (comp/computed p {:on-delete delete-person}))) people)))))

(def ui-person-list (comp/factory PersonList))

#_(defsc AllPeople [this {:person/keys [id name]}]
    {:query [:person/id :person/name]
     :ident (fn [] [:person/id id])}
    (dom/div
      (dom/ul
        (map (fn [p] )))))

(defsc Root [this {:keys [friends enemies plot-data]}]
  {:query [{:friends (comp/get-query PersonList)}
           {:enemies (comp/get-query PersonList)}
           :plot-data]}
  (prn "plot-data?" (some? plot-data))
  (dom/div
    ;; (dom/h1 "Friends")
    ;; (when friends
    ;;   (ui-person-list friends))
    ;; (dom/h1 "Enemies")
    ;; (when enemies
    ;;   (ui-person-list enemies))
    ;; (dom/button {:onClick
    ;;              #(comp/transact! this [(mutations/fetch-plot-data)])} "reload")
    (graph/yearly-value-chart {:label     "Temperature"
                               :x-step    2
                               :plot-data plot-data})))

(comment
  (js/console.log "hi from emacs")
  (comp/get-initial-state Root)
  )
