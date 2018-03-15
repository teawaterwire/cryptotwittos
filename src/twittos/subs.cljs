(ns twittos.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 :get
 (fn [db [_ & ks]]
   (get-in db ks)))
