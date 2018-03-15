(ns twittos.events
  (:require [re-frame.core :as rf]
            [twittos.db :as db]
            [ajax.core :as ajax]
            [cljs-web3.core :as web3-core]
            [cljs-web3.eth :as web3-eth]
            [clojure.string :as str]
            ; ["truffle-contract" :as contract]
            ))

(rf/reg-event-db
 :init
 (fn []
   db/default-db))

(rf/reg-event-db
 :set
 (fn [db [_ & args]]
   (assoc-in db (butlast args) (last args))))

(rf/reg-event-db
 :inc
 (fn [db [_ k]]
   (update db k inc)))

(rf/reg-event-fx
 :search-twitter
 (fn [{{:keys [query]} :db}]
   {:http-xhrio {:method :get
                 :uri (str db/twitter-search-url query)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:assoc-twittos :results]}}))

(rf/reg-event-fx
 :lookup-twitter
 (fn [_ [_ k ids]]
   {:http-xhrio {:method :get
                 :uri (str db/twitter-lookup-url (str/join "," (map #(.toString % 10) ids)))
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:assoc-twittos k]}}))

(rf/reg-event-fx
 :get-trophies
 (fn [{:keys [db]} ev]
   (console.log ev)
   {:web3/call {:web3 (:web3 db)
                :fns [{:instance (:instance db)
                       :fn :get-twitto-ids
                       :args [false]
                       :on-success [:lookup-twitter :trophies]
                       :on-error [:get-trophies-error]}]}}))

(rf/reg-event-db
 :assoc-twitto
 (fn [db [_ id-str twitto]]
   (let [[stealer price] twitto]
     (assoc-in db [:twittos id-str] {:stealer stealer :price price}))))

(rf/reg-event-fx
 :lookup-twitto
 (fn [{:keys [db]} [_ twitter-id-str]]
   {:web3/call {:web3 (:web3 db)
                :fns [{:instance (:instance db)
                       :fn :twittos
                       :args [twitter-id-str]
                       :on-success [:assoc-twitto twitter-id-str]}]}}))

(rf/reg-event-fx
 :assoc-twittos
 (fn [{db :db} [_ k twittos]]
   (let [twittos' (map #(select-keys % [:id_str :screen_name :name :description :profile_image_url_https]) twittos)]
     {:db (assoc db k twittos')
      :dispatch-n (for [id-str (map :id_str twittos')] [:lookup-twitto id-str])})))

; (rf/reg-event-db
;  :assoc-twittos
;  (fn [db [_ k twittos]]
;    (let [twittos' (map #(select-keys % [:id_str :screen_name :name :description :profile_image_url_https]) twittos)]
;      (assoc db k twittos'))))

(rf/reg-event-fx
 :steal
 (fn [{:keys [db]} [_ id-str]]
   (let [price (-> (get-in db [:next-prices id-str])
                   (web3-core/to-wei "finney"))]
     {:web3/call {:web3 (:web3 db)
                  :fns [{:instance (:instance db)
                         :fn :steal
                         :args [id-str price]
                         :tx-opts {:from (web3-eth/coinbase (:web3 db))
                                   :value (get-in db [:twittos id-str :price] 0)}
                         :on-tx-success [:get-trophies]
                         :on-tx-error [:steal-error]
                         :on-tx-receipt [:get-trophies]}]}})))

(rf/reg-event-fx
 :get-contract
 (fn []
   {:http-xhrio {:method :get
                 :uri "CryptoTwittos.json"
                 ; :response-format (ajax/json-response-format {:raw true})
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:init-contract]}}))

(rf/reg-event-fx
 :init-contract
 (fn [{{:keys [web3 network-id] :as db} :db} [_ artifact]]
   (let [{:keys [abi contractName networks]} artifact
         address (get-in networks [network-id :address])
         instance (web3-eth/contract-at web3 abi address)]
     {:db (assoc db :instance instance)
      :dispatch [:get-trophies]})))

(rf/reg-event-fx
 :get-twittos
 (fn [{:keys [db]}]
   {:web3/call {:web3 (:web3 db)
                :fns [{:instance (:instance db)
                       :fn :get-twitto-ids
                       :args [true]
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
