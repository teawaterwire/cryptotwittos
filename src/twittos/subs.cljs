(ns twittos.subs
  (:require [re-frame.core :as rf]
            [cljs-web3.core :as web3-core]))

(rf/reg-sub
 :get
 (fn [db [_ & ks]]
   (get-in db ks)))

(rf/reg-sub
 :get-trophies-value
 :<- [:get :trophies]
 :<- [:get :twittos]
 (fn [[trophies twittos]]
   (let [get-price (fn [id]
                     (-> (get-in twittos [id :price])
                         (web3-core/from-wei "finney")
                         (js/parseInt)))
         ids (map :id_str trophies)
         prices (map get-price ids)]
     (str (reduce + prices) " 💸 Finney"))))
