(ns twittos.views
  (:require [re-frame.core :as rf]))

(defn search-bar []
  [:div.ui.huge.fluid.input.action
   [:input {:type "text" :placeholder "Search for Twittos"
            :value @(rf/subscribe [:get :query])
            :on-change #(rf/dispatch [:set :query (.. % -target -value)])}]
   [:button.ui.icon.button.purple
    {:on-click #(rf/dispatch [:search-twitter])}
    [:i.icon.search]]])

(defn results-cards []
  [:div.ui.cards
   (for [{:keys [id_str name screen_name description profile_image_url_https]}
         @(rf/subscribe [:get :results])
         :let [image-url (.replace profile_image_url_https "_normal" "")]]
     ^{:key id_str}
     [:div.card
      [:div.content
       [:img.ui.right.floated.image.tiny {:src image-url}]
       [:div.header name]
       [:div.meta "@" screen_name]
       [:div.description description]]])])

(defn results-items []
  [:div.ui.items
   (for [{:keys [id_str name screen_name description profile_image_url_https]}
         @(rf/subscribe [:get :results])
         :let [image-url (.replace profile_image_url_https "_normal" "")]]
     ^{:key id_str}
     [:div.item
      [:a.ui.image.tiny
       [:img {:src image-url}]]
      [:div.content
       [:div.header name]
       [:div.meta "@" screen_name]
       [:div.description description]
       [:div.extra
        [:div.ui.right.floated.purple.button
         "Steal"
         [:i.icon.right.user.secret]]
        [:div.ui.label.black (rand-int 3123123123) " Gwei"]]]])])

(defn main []
  [:div.ui.text.container.mt4
   [:h1.ui.dividing.header "CryptoTwittos"]
   [:p "lol: " @(rf/subscribe [:get :lol])]
   [:div.ui.button.secondary {:on-click #(rf/dispatch [:inc :lol])} "Inc"]
   [:div.ui.button.primary {:on-click #(rf/dispatch [:get-contract])} "Init contract"]
   [:div.ui.button {:on-click #(rf/dispatch [:get-twittos])} "Get Twittos"]
   [:hr]
   [search-bar]
   [results-items]
   ])
