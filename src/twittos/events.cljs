(ns twittos.events
  (:require [re-frame.core :as rf]
            [twittos.db :as db]))

(rf/reg-event-db
 :init
 (fn []
   (db/default-db)))

(rf/reg-event-db
 :inc
 (fn [db [_ k]]
   (update db k inc)))
