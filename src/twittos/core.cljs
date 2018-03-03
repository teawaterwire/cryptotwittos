(ns twittos.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [twittos.views :as views]
            [twittos.subs]
            [twittos.events]))

(defn mount! []
  (js/console.log "Mounting...")
  (reagent/render [views/main] (js/document.getElementById "app")))

(defn ^:export init []
  (rf/dispatch-sync [:init])
  (mount!))
