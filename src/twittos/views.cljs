(ns twittos.views
  (:require [re-frame.core :as rf]
            [cljs-web3.core :as web3-core]))

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

(defn twitto-item [{:keys [id_str name screen_name description profile_image_url_https]} stealable?]
  [:div.item
   [:a.ui.image.tiny
    [:img {:src (.replace profile_image_url_https "_normal" "")}]]
   [:div.content
    [:div.header name]
    [:div.meta "@" screen_name]
    [:div.description description]
    [:div.extra
     (let [price-wei @(rf/subscribe [:get :twittos id_str :price])
           price (web3-core/from-wei price-wei "finney")]
       [:div.ui.label.black (if (= (.toString price) "0") "FREE" (str price " 💸Finney"))])
     (if stealable?
       [:div.ui.action.input
        [:input {:type "text" :placeholder "Set next price in Finney"
                 :on-change #(rf/dispatch [:set :next-prices id_str (.. % -target -value)])
                 :value @(rf/subscribe [:get :next-prices id_str])}]
        [:button.ui.purple.right.labeled.icon.button
         {:on-click #(rf/dispatch [:steal id_str])}
         [:i.icon.right.user.secret]
         "Steal"]])]]])

(defn results-items []
  [:div.ui.items
   (let [trophies @(rf/subscribe [:get :trophies])]
     (for [{:keys [id_str] :as result}
           @(rf/subscribe [:get :results])]
       ^{:key id_str}
       [twitto-item result (not (some #{result} trophies))]))])

(defn search-col []
  [:div.column
   [:h1.ui.dividing.header.purple "CryptoTwittos"]
   [:div.ui.button.primary {:on-click #(rf/dispatch [:get-contract])} "Init contract"]
   [:div.ui.button {:on-click #(rf/dispatch [:get-twittos])} "Get Twittos"]
   [search-bar]
   [results-items]])

(defn trophies-col []
  [:div.column
   [:h1.ui.dividing.header "Your Trophies"]
   [:div.ui.button {:on-click #(rf/dispatch [:get-trophies])} "Get Trophies"]
   [:div.ui.items
    (for [{:keys [id_str] :as trophy} @(rf/subscribe [:get :trophies])]
      ^{:key id_str}
      [twitto-item trophy false])]])

(defn steals-col []
  [:div.column
   [:h1.ui.dividing.header "Live Steals"]])

(defn main []
  [:div.ui.stackable.three.column.grid
   [:div.row
    [search-col]
    [trophies-col]
    [steals-col]
    [:div.column]]])
