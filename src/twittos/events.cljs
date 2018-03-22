(ns twittos.events
  (:require [re-frame.core :as rf]
            [twittos.db :as db]
            [ajax.core :as ajax]
            [cljs-web3.core :as web3-core]
            [cljs-web3.eth :as web3-eth]
            [clojure.string :as str]))

(rf/reg-event-db
 :init
 (fn []
   db/default-db))

(rf/reg-event-db
 :set
 (fn [db [_ & args]]
   (assoc-in db (butlast args) (last args))))

;; -------------------
;; CONTRACT
;; -------------------

(rf/reg-event-fx
 :get-contract
 (fn []
   {:http-xhrio {:method :get
                 :uri "CryptoTwittos.json"
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:init-contract]}}))

(rf/reg-event-fx
 :init-contract
 (fn [{{:keys [web3 network-id] :as db} :db} [_ artifact]]
   (let [{:keys [abi contractName networks]} artifact
         address (get-in networks [network-id :address])
         instance (web3-eth/contract-at web3 abi address)]
     {:db (assoc db :instance instance)
      :web3/call {:web3 web3
                  :fns [{:instance instance
                         :fn :owner
                         :on-success [:set :owner]}]}
      :dispatch-n [[:get-trophies]
                   [:watch-steals]]})))

;; -------------------
;; TROPHIES
;; -------------------

(rf/reg-event-fx
 :get-trophies
 (fn [{:keys [db]}]
   {:web3/call {:web3 (:web3 db)
                :fns [{:instance (:instance db)
                       :fn :get-twitto-ids
                       :args [false]
                       :on-success [:get-trophies-success]
                       :on-error [:get-trophies-error]}]}}))

(rf/reg-event-fx
 :get-trophies-success
 (fn [{db :db} [_ ids]]
   (let [id-strs (reverse (map str ids))]
     {:db (assoc db :trophies id-strs)
      :dispatch [:lookup-twitter id-strs]
      :dispatch-n (for [id-str id-strs] [:lookup-twitto id-str])})))

;; -------------------
;; LOOKUP
;; -------------------

(rf/reg-event-fx
 :lookup-twitter
 (fn [{db :db} [_ id-strs]]
   (let [twitteros-id-strs-set (set (keys (:twitteros db)))
         id-strs-remaining (remove twitteros-id-strs-set id-strs)]
     (if (empty? id-strs-remaining) {}
       {:http-xhrio {:method :get
                     :uri (str db/twitter-lookup-url (str/join "," id-strs-remaining))
                     :response-format (ajax/json-response-format {:keywords? true})
                     :on-success [:store-twitteros]}}))))

(rf/reg-event-fx
 :store-twitteros
 (fn [{db :db} [_ twitteros]]
   (let [twitteros' (map #(select-keys % [:id_str :screen_name :name :description :profile_image_url_https]) twitteros)
         twitteros'' (zipmap (map :id_str twitteros') twitteros')]
     {:db (update db :twitteros merge twitteros'')})))

(rf/reg-event-fx
 :lookup-twitto
 (fn [{:keys [db]} [_ twitter-id-str]]
   {:web3/call {:web3 (:web3 db)
                :fns [{:instance (:instance db)
                       :fn :twittos
                       :args [twitter-id-str]
                       :on-success [:assoc-twitto twitter-id-str]}]}}))

(rf/reg-event-db
 :assoc-twitto
 (fn [db [_ id-str twitto]]
   (let [[stealer price] twitto]
     (assoc-in db [:twittos id-str] {:stealer stealer :price price}))))

;; -------------------
;; SEARCH
;; -------------------

(rf/reg-event-fx
 :search-twitter
 (fn [{{:keys [query]} :db}]
   {:dispatch [:set :searching? true]
    :http-xhrio {:method :get
                 :uri (str db/twitter-search-url query)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:search-twitter-success]
                 :on-error [:set :searching? false]}}))

(rf/reg-event-fx
 :search-twitter-success
 (fn [{db :db} [_ twitteros]]
   (let [id-strs (map :id_str twitteros)]
     {:db (assoc db :results id-strs)
      :dispatch-n (concat [[:set :searching? false]
                           [:store-twitteros twitteros]]
                          (for [id-str id-strs] [:lookup-twitto id-str]))})))

;; -------------------
;; STEAL
;; -------------------

(rf/reg-event-fx
 :steal
 (fn [{:keys [db]} [_ id-str]]
   (let [price (-> (get-in db [:next-prices id-str])
                   (web3-core/to-wei "finney"))]
     {:dispatch [:set :stealing? id-str true]
      :web3/call {:web3 (:web3 db)
                  :fns [{:instance (:instance db)
                         :fn :steal
                         :args [id-str price]
                         :tx-opts {:value (get-in db [:twittos id-str :price] 0)}
                         :on-tx-success [:get-trophies]
                         :on-tx-receipt [:steal-end id-str]}]}})))

(rf/reg-event-fx
 :steal-end
 (fn [_ [_ id-str]]
   {:dispatch-n [[:set :stealing? id-str false]]}))

;; -------------------
;; EVENTS
;; -------------------

(rf/reg-event-fx
 :watch-steals
 (fn [{:keys [db]}]
   {:web3/watch-events {:events [{:id :steals-watcher
                                  :event :stealEvent
                                  :instance (:instance db)
                                  :block-filter-opts {:from-block 0 :to-block "latest"}
                                  :on-success [:new-steal]}]}}))

(rf/reg-event-fx
 :new-steal
 (fn [{db :db} [_ {:keys [id price new-price owner] :as steal} {:keys [block-hash]}]]
   (let [new-steals (conj (:steals db)
                          (merge steal {:block block-hash
                                        :id (str id)
                                        :price (str price)
                                        :new-price (str new-price)}))
         new-db (assoc db :steals (->> new-steals (distinct) (take 20)))
         id-strs (distinct (map #(str (:id %)) (:steals new-db)))]
     {:db new-db
      :web3/call {:web3 (:web3 db)
                  :fns [{:fn web3-eth/get-block
                         :args [block-hash]
                         :on-success [:block-details]}
                        {:fn web3-eth/coinbase
                         :on-success [:refresh-trophies owner]}]}
      :dispatch-debounce [{:id :lookup-twitter
                           :timeout 400
                           :action :dispatch-n
                           :event (concat [[:lookup-twitter id-strs]]
                                          (for [id-str id-strs] [:lookup-twitto id-str]))}]})))

(rf/reg-event-fx
 :refresh-trophies
 (fn [_ [_ owner coinbase]]
   (when (= owner coinbase)
     {:dispatch-debounce [{:id :refresh-trophies
                           :timeout 500
                           :action :dispatch
                           :event [:get-trophies]}]})))

(rf/reg-event-db
 :block-details
 (fn [db [_ {:keys [hash timestamp]}]]
   (assoc-in db [:blocks hash] timestamp)))

;; -------------------
;; OWNER ONLY
;; -------------------

(rf/reg-event-fx
 :pause
 (fn [{db :db} [_ pause?]]
   {:web3/call {:web3 (:web3 db)
                :fns [{:instance (:instance db)
                       :fn (if pause? :pause :unpause)}]}}))

(rf/reg-event-fx
 :withdraw
 (fn [{db :db} [_ pause?]]
   {:web3/call {:web3 (:web3 db)
                :fns [{:instance (:instance db)
                       :fn :withdraw}]}}))
