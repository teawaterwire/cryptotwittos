(ns twittos.owner
  (:require [re-frame.core :as rf]))

(defn owner-only []
  (if (and
       (exists? js/web3)
       (= @(rf/subscribe [:get :owner]) js/web3.eth.coinbase))
    [:div.row
     [:div.column.grey
      [:button.ui.orange.button
       {:on-click #(rf/dispatch [:pause true])}
       "Pause"]
      [:button.ui.orange.button
       {:on-click #(rf/dispatch [:pause false])}
       "UnPause"]
      [:button.ui.green.button.float-r
       {:on-click #(rf/dispatch [:withdraw])}
       "Withdraw"]]]))
