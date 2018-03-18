(ns twittos.views
  (:require [re-frame.core :as rf]
            [cljs-web3.core :as web3-core]
            [twittos.timer :refer [clock]]))

(defn search-bar []
  [:div.ui.massive.fluid.input.action
   [:input {:type "text" :placeholder "Search for Twittos"
            :value @(rf/subscribe [:get :query])
            :on-key-down #(if (= (.-which %) 13) (rf/dispatch [:search-twitter]))
            :on-change #(rf/dispatch [:set :query (.. % -target -value)])}]
   [:button.ui.icon.button.orange
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

(defn steal-tx [owner stealer price block]
  (let [->icon (fn [u] [:span.ui.circular.horizontal.label.empty {:style {:background-color (str "#" (subs u 2 8))}}])
        [icon-owner icon-stealer] (map ->icon  [owner stealer])]
    [:div
     [:p
      icon-stealer
      [:i.angle.double.right.icon]
      [:span.ui.horizontal.label.orange.tiny (str (web3-core/from-wei price "finney")) " Finney"]
      [:i.angle.double.right.icon]
      icon-owner]
     [:div @(rf/subscribe [:time-block block])]]))

(defn twitto-item [{:keys [id_str name screen_name description profile_image_url_https owner stealer price stealable?]}]
  [:div.column
   [:div.ui.card.fluid
    [:div.ui.image
     [:div.ui.right.ribbon.label.green @(rf/subscribe [:get-price id_str])]
     [:img {:src (.replace profile_image_url_https "_normal" "")}]]
    [:div.content
     [:div.header name]
     [:div.meta.orange-text "@" screen_name]]]])

(defn twitto-item' [{:keys [id_str name screen_name description profile_image_url_https owner stealer price block stealable?]}]
  [:div.item
   [:a.ui.image.tiny
    [:img {:src (.replace profile_image_url_https "_normal" "")}]]
   [:div.content
    [:div.header name]
    [:div.ui.right.floated.label.green @(rf/subscribe [:get-price id_str])]
    [:div.meta.orange-text "@" screen_name]
    (if-not stealer
      [:div.description description])
    [:div.extra
     (if stealer
       [steal-tx owner stealer price block])
     (if stealable?
       [:div.ui.action.input.fluid
        [:input {:type "text" :placeholder "Set next price in Finney"
                 :on-change #(rf/dispatch [:set :next-prices id_str (.. % -target -value)])
                 :value @(rf/subscribe [:get :next-prices id_str])}]
        [:button.ui.orange.button
         {:on-click #(rf/dispatch [:steal id_str])
          :class (if @(rf/subscribe [:get :stealing? id_str]) "loading")
          :disabled @(rf/subscribe [:disabled? id_str])}
         "Steal"]])]]])

(defn results-items []
  [:div.ui.divided.items
  ; [:div.ui.two.column.grid
   (for [{:keys [id_str] :as result} @(rf/subscribe [:results])]
     ^{:key id_str}
     [twitto-item' result])])

(defn search-col []
  [:div.column
   [:h2.ui.dividing.header
    [:img.ui.image.logo {:src "/img/twittos.png"}]
    "Crypto" [:span.orange-text "Twittos"]
    [:div.ui.sub.header "Steal 'Em All"]]
   [search-bar]
   [results-items]])

(defn trophies-col []
  [:div.column
   [:h2.ui.dividing.header
    "Your Trophies"
    [:div.ui.label.green.float-r.large
     [:span.light "TOTAL VALUE: "]
     [:strong @(rf/subscribe [:trophies-value])]]
    [:div.ui.sub.header
     "1000 Finney (F) = 1 Ether"]]
   [:div.ui.cards.two.column.grid
    (for [{:keys [id_str] :as trophy} @(rf/subscribe [:trophies])
          :when (some? id_str)]
      ^{:key id_str}
      [twitto-item trophy])]])

(defn steals-col []
  [:div.column
   [:h2.ui.dividing.header
    "Live Steals"
    [:div.ui.sub.header
     [clock]]]
   [:div.ui.divided.items
    (for [{:keys [id_str new-price] :as stolen-twitto} @(rf/subscribe [:stolen-twittos])
          :when (some? id_str)]
      ^{:key (str id_str new-price)}
      [twitto-item' stolen-twitto])]])

(defn main []
  [:div.ui.stackable.three.column.grid
   [:div.row
    [search-col]
    [trophies-col]
    [steals-col]
    [:div.column]]])
