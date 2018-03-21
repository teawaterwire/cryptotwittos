(ns twittos.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [twittos.views :as views]
            [twittos.subs]
            [twittos.events]
            [day8.re-frame.http-fx]
            [district0x.re-frame.web3-fx]
            [re-frame-fx.dispatch]))

(defn mount! []
  (rf/clear-subscription-cache!)
  (reagent/render [views/main]
                  (js/document.getElementById "app")))

(defn ^:export init []
  (when (exists? js/web3)
    (rf/dispatch-sync [:init])
    (rf/dispatch [:get-contract]))
  (mount!))
