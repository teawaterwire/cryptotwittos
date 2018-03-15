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
     (str (reduce + prices) " 💸Finney"))))

(rf/reg-sub
 :get-price
 (fn [[_ id-str]]
   (rf/subscribe [:get :twittos id-str :price]))
 (fn [price-wei]
   (let [price (str (web3-core/from-wei price-wei "finney"))]
     (if (= price "0") "FREE" (str price " 💸Finney")))))

(rf/reg-sub
 :disabled?
 (fn [[_ id-str]]
   [(rf/subscribe [:get :twittos id-str :price])
    (rf/subscribe [:get :next-prices id-str])])
 (fn [[price-wei next-price]]
   (try
     (not
      (.. (web3-core/to-big-number next-price)
          (gt (web3-core/from-wei price-wei "finney"))))
     (catch js/Object e true))))
