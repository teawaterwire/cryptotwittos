(ns twittos.views
  (:require [re-frame.core :as rf]))

(defn main []
  [:div.ui.text.container.mt4
   [:h1.ui.dividing.header "CryptoTwittos"]
   [:p "lol: " @(rf/subscribe [:get :lol])]
   [:div.ui.button {:on-click #(rf/dispatch [:inc :lol])} "Inc"]])
