(ns twittos.timer
  (:require [reagent.core :as r]))

(defonce timer (r/atom (js/Date.)))

(defonce time-updater (js/setInterval
                       #(reset! timer (js/Date.)) 1000))

(defn clock []
  (let [time-str (-> @timer .toTimeString (clojure.string/split " ") first)]
    [:span
     (.. @timer (toDateString)) " — "
     time-str]))
