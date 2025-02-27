(ns sherlockbench.logic
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.pprint :refer [pprint]]
            [hodgepodge.core :refer [local-storage clear!]]
            [cljs.core.async :refer [<!]]
            [reitit.frontend.easy :as reitit-easy])
  )

;; Functions in this file will be called from core.cljs.
;; Usually from the global event handler: https://replicant.fun/event-handlers/

(defn start-run [store run-id]
  (go
    (let [response (<! (http/post "http://localhost:3000/api/start-run"
                                  {:with-credentials? false
                                   :json-params (cond-> {:client-id "sherlockbench-testme"}
                                                  (not (nil? run-id)) (assoc :existing-run-id run-id))}))
          {{:keys [run-id run-type benchmark-version attempts]} :body} response
          attempts-named (map
                          #(assoc %1 :problem-name (str "Problem " %2))
                          attempts (range 1 js/Infinity))
          run-data {:run-id run-id
                    :run-type run-type
                    :benchmark-version benchmark-version
                    :attempts attempts-named}]

      (prn (str "Starting " run-type " benchmark with run-id: " run-id))

      ;; update the atom
      (reset! store run-data)
      ;; update the localStorage
      (assoc! local-storage (str "run-" run-id) run-data)
      ;; redirect to the index page
      (reitit-easy/push-state :index {:run-id run-id} {})
      )))

(defn find-attempt-by-id [attempts attempt-id]
  (first (filter #(= (:attempt-id %) attempt-id) attempts)))

(defn if-run-complete [store run-id]
  (let [attempts (:attempts @store)]
    (when (every? #(= true (:completed %)) attempts)
      (let [response (<! (http/post "http://localhost:3000/api/complete-run"
                                    {:with-credentials? false
                                     :json-params {:run-id run-id}}))]
        true ;; todo finish this

        )

      )

    ))
