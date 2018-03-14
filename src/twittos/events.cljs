(ns twittos.events
  (:require [re-frame.core :as rf]
            [twittos.db :as db]
            [ajax.core :as ajax]
            [cljs-web3.eth :as web3-eth]
            ; ["truffle-contract" :as contract]
            ))

(rf/reg-event-db
 :init
 (fn []
   db/default-db))

(rf/reg-event-db
 :set
 (fn [db [_ k v]]
   (assoc db k v)))

(rf/reg-event-db
 :inc
 (fn [db [_ k]]
   (update db k inc)))

(rf/reg-event-fx
 :get-contract
 (fn []
   {:http-xhrio {:method :get
                 :uri "CryptoTwittos.json"
                 ; :response-format (ajax/json-response-format {:raw true})
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:init-contract]}}))

(rf/reg-event-db
 :init-contract
 (fn [{:keys [web3 network-id] :as db} [_ artifact]]
   ; (let [abstr (contract artifact)]
   ;   (.setProvider abstr (.-currentProvider web3))
   ;   (.. abstr (deployed) (then #(rf/dispatch [:set :instance %])))
   ;   db)
   (let [{:keys [abi contractName networks]} artifact
         address (get-in networks [network-id :address])
         instance (web3-eth/contract-at web3 abi address)]
     ; (console.log contractName (get-in networks [network-id :address]))
     ; (console.log instance)
     (assoc db :instance instance))
   ))

(rf/reg-event-fx
 :get-twittos
 (fn [{:keys [db]}]
   ; (console.log 44)
   {:web3/call {:web3 (:web3 db)
                :fns [{
                       :instance (:instance db)
                       :fn :get-twitto-ids
                       ; :fn cljs-web3.eth/accounts
                       ; :args []
                       :on-success [:get-twittos-success]
                       :on-error [:get-twittos-error]}]}}))
(rf/reg-event-db
 :get-twittos-error
 (fn [db [_ error]]
   (console.log error "ERRIR")
   db))

(rf/reg-event-db
 :get-twittos-success
 (fn [db [_ twittos]]
   (console.log twittos (map #(.toNumber %) twittos) "TWITTOS")
   (assoc db :twittos twittos)))
