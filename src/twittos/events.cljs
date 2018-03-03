(ns twittos.events
  (:require [re-frame.core :as rf]
            [twittos.db :as db]
            ["web3" :as Web3]))

(rf/reg-event-db
 :init
 (fn []
   db/default-db))

(rf/reg-event-db
 :inc
 (fn [db [_ k]]
   (update db k inc)))

(rf/reg-event-db
 :init-web3
 (fn [{:keys [web3] :as db}]
   (let [web3 (Web3. web3.currentProvider)]
     (assoc db :web3 web3))))
