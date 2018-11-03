(ns twittos.effects
  (:require [re-frame.core :as rf]
            [cljs-web3.core :as web3-core]
            [cljs-web3.eth :as web3-eth]
            ["portis" :refer (PortisProvider)]))

(defonce api-key "22126369e4329dcba29d062833d9d553")

(rf/reg-fx
 :enable-web3
 (fn [next-dispatch]
   (let [modern? (exists? js/ethereum)
         legacy? (exists? js/web3)
         disp! #(rf/dispatch next-dispatch)]
     (cond
       modern? (.. (.enable ^js js/ethereum)
                   (then #(do (set! js/web3 (new js/Web3 js/ethereum)) (disp!)))
                   (catch #(do (set! js/web3 nil) (disp!))))
       legacy? (do
                 (set! js/web3 (new js/Web3 (web3-core/current-provider js/web3)))
                 (disp!))
       ;; Portis fallback
       :else (do
               (set! js/web3 (new js/Web3 (PortisProvider. #js {:apiKey api-key :network "mainnet"})))
               (web3-eth/accounts
                 js/web3
                 #(disp!)))))))

(rf/reg-cofx
 :web3
 (fn [{:keys [db] :as coeffects}]
   (let [w3 js/web3 ; Will call (web3-core/web3) when fixed
         main-account (first (web3-eth/accounts w3))
         ;; Set default account for later txns
         _ (set! js/web3.eth.defaultAccount main-account)
         web3-details
         {:web3 w3
          :network-id (or (keyword (web3-core/version-network w3)) :1)
          :coinbase (web3-eth/default-account w3)}]
     (assoc coeffects :web3-details web3-details))))
