(ns twittos.core
  (:require [cljsjs.web3]
            [reagent.core :as reagent]
            [re-frame.core :as rf]
            [twittos.views :as views]
            [twittos.subs]
            [twittos.events]
            [twittos.effects]
            [day8.re-frame.http-fx]
            [district0x.re-frame.web3-fx]
            [re-frame-fx.dispatch]))

(defn mount! []
  (rf/clear-subscription-cache!)
  (reagent/render [views/main]
                  (js/document.getElementById "app")))

(defn ^:export init []
  (rf/dispatch-sync [:init])
  (mount!))
