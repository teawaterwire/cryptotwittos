(ns twittos.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [twittos.views :as views]
            [twittos.subs]
            [twittos.events]
            [day8.re-frame.http-fx]
            [district0x.re-frame.web3-fx]))

(defn mount! []
  (rf/clear-subscription-cache!)
  (js/console.log "Mounting...")
  (reagent/render [views/main] (js/document.getElementById "app"))
  (js/console.log "...done!"))

(defn ^:export init []
  (rf/dispatch-sync [:init])
  (mount!))
