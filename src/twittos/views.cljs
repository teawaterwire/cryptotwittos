(ns twittos.views
  (:require [re-frame.core :as rf]
            [cljs-web3.core :as web3-core]))

(defn search-bar []
  [:div.ui.huge.fluid.input.action
   [:input {:type "text" :placeholder "Search for Twittos"
            :value @(rf/subscribe [:get :query])
            :on-key-down #(if (= (.-which %) 13) (rf/dispatch [:search-twitter]))
            :on-change #(rf/dispatch [:set :query (.. % -target -value)])}]
   [:button.ui.icon.button.purple
    {:on-click #(rf/dispatch [:search-twitter])
     :class (if @(rf/subscribe [:get :searching?]) "loading")}
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

(defn steal-tx [owner stealer price]
  (let [[owner stealer] (map #(subs % 2 8) [owner stealer])
        ->icon (fn [u] [:span.ui.circular.label.empty {:style {:background-color (str "#" (subs u 2 8))}}])
        [icon-owner icon-stealer] (map ->icon  [owner stealer])]
    [:div
     icon-stealer
     [:span.ui.circular.label (str (web3-core/from-wei price "finney")) " 💸Finney"]
     icon-owner]))

(defn twitto-item [{:keys [id_str name screen_name description profile_image_url_https owner stealer price stealable?]}]
  [:div.item
   [:a.ui.image.tiny
    [:img {:src (.replace profile_image_url_https "_normal" "")}]]
   [:div.content
    [:div.header name]
    [:div.meta "@" screen_name]
    [:div.description description]
    [:div.extra
     (if stealer
       [steal-tx owner stealer price])
     [:div.ui.label.tag.black.large @(rf/subscribe [:get-price id_str])]
     (if stealable?
       [:div.ui.action.input
        [:input {:type "text" :placeholder "Set next price in Finney"
                 :on-change #(rf/dispatch [:set :next-prices id_str (.. % -target -value)])
                 :value @(rf/subscribe [:get :next-prices id_str])}]
        [:button.ui.purple.right.labeled.icon.button
         {:on-click #(rf/dispatch [:steal id_str])
          :class (if @(rf/subscribe [:get :stealing? id_str]) "loading")
          :disabled @(rf/subscribe [:disabled? id_str])}
         [:i.icon.right.user.secret]
         "Steal"]])]]])

(defn results-items []
  [:div.ui.items
   (for [{:keys [id_str] :as result} @(rf/subscribe [:results])]
     ^{:key id_str}
     [twitto-item result])])

(defn search-col []
  [:div.column
   [:h1.ui.dividing.header.purple "CryptoTwittos"]
   [search-bar]
   [results-items]])

(defn trophies-col []
  [:div.column
   [:h1.ui.dividing.header
    "Your Trophies"
    [:span.ui.label.black @(rf/subscribe [:trophies-value])]]
   [:div.ui.items
    (for [{:keys [id_str] :as trophy} @(rf/subscribe [:trophies])
          :when (some? id_str)]
      ^{:key id_str}
      [twitto-item trophy])]])

(defn steals-col []
  [:div.column
   [:h1.ui.dividing.header "Live Steals"]
   [:div.ui.items
    (for [{:keys [id_str new-price] :as stolen-twitto} @(rf/subscribe [:stolen-twittos])
          :when (some? id_str)]
      ^{:key (str id_str new-price)}
      [twitto-item stolen-twitto])]])

(defn main []
  [:div.ui.stackable.three.column.grid
   [:div.row
    [search-col]
    [trophies-col]
    [steals-col]
    [:div.column]]])
