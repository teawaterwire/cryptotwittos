(ns twittos.subs
  (:require [re-frame.core :as rf]
            [clojure.string :as str]
            [cljs-web3.eth :as web3-eth]
            [cljs-web3.core :as web3-core]))

(rf/reg-sub
 :get
 (fn [db [_ & ks]]
   (get-in db ks)))

(rf/reg-sub
 :trophies-value
 :<- [:get :trophies]
 :<- [:get :twittos]
 (fn [[trophies twittos]]
   (let [get-price (fn [id]
                     (-> (get-in twittos [id :price])
                         (web3-core/from-wei "finney")
                         (js/parseInt)))
         prices (map get-price trophies)]
     (str (reduce + prices) " F"))))

(rf/reg-sub
 :trophies
 :<- [:get :trophies]
 :<- [:get :stolen-trophies]
 :<- [:get :twitteros]
 (fn [[trophies stolen-trophies twitteros]]
   (if-not (empty? twitteros)
     (->> trophies
          (remove stolen-trophies)
          (map #(get twitteros %))))))

(rf/reg-sub
 :results
 :<- [:get :results]
 :<- [:get :twitteros]
 :<- [:get :trophies]
 (fn [[results twitteros trophies]]
   (if-not (empty? twitteros)
     (map #(-> (get twitteros %)
               (assoc :stealable? (not (some #{%} trophies))))
          results))))

(rf/reg-sub
 :stolen-twittos
 :<- [:get :steals]
 :<- [:get :twitteros]
 (fn [[steals twitteros]]
   (if-not (empty? twitteros)
     (map #(merge (get twitteros (str (:id %))) %)
          steals))))

(rf/reg-sub
 :time-block
 :<- [:get :blocks]
 (fn [blocks [_ block]]
   (let [stamp (-> (get blocks block)
                   (js/parseInt)
                   (* 1000))
         date (js/Date. stamp)]
     (str (.. date (toDateString))
          " — "
          (-> date (.toTimeString) (str/split " ") (first))))))

(rf/reg-sub
 :get-price
 (fn [[_ id-str]]
   (rf/subscribe [:get :twittos id-str :price]))
 (fn [price-wei]
   (let [price (str (web3-core/from-wei price-wei "finney"))]
     (if (= price "0") "FREE" (str price " F")))))

(rf/reg-sub
 :disabled?
 (fn [[_ id-str]]
   [(rf/subscribe [:get :twittos id-str :price])
    (rf/subscribe [:get :next-prices id-str])])
 (fn [[price-wei next-price]]
   (try
     (not
      (-> ^js (web3-core/to-big-number next-price)
          (.gt (web3-core/from-wei price-wei "finney"))))
     (catch js/Object e true))))

(rf/reg-sub
 :owner?
 :<- [:get :web3]
 :<- [:get :owner]
 (fn [[web3 owner]]
   (if web3
     (= owner (web3-eth/coinbase web3)))))
