(ns twittos.effects
  (:require [re-frame.core :as rf]
            [cljs-web3.core :as web3-core]
            [cljs-web3.eth :as web3-eth]
            ["@portis/web3/es" :default Portis]))

(defonce api-key "acff1db8-4af7-4dd8-8626-adeb65fd7ebd")

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
               (let [portis (new Portis api-key "mainnet")]
                 (set! js/web3 (new js/Web3 portis.provider))
                 (.enable portis.provider)
                 (.onLogin portis (fn [] (web3-core/version-network js/web3 #(disp!))))))))))

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
