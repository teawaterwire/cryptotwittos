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

(defn ->hex
    ([addr] (->hex addr false))
    ([addr s?] (str (if s? "#") (subs addr 2 8))))

(defn steal-tx [owner stealer price]
  (if stealer [:div
               [:span.ui.label {:style {:background-color (->hex owner true)}} (->hex owner)]
               [:span.ui.label (str price)]
               [:span.ui.label (->hex stealer)]]))

(defn twitto-item [{:keys [id_str name screen_name description profile_image_url_https owner stealer price stealable?]}]
  [:div.item
   [:a.ui.image.tiny
    [:img {:src (.replace profile_image_url_https "_normal" "")}]]
   [:div.content
    [:div.header name]
    [:div.meta "@" screen_name]
    (if-not stealer [:div.description description])
    [:div.extra
     [steal-tx owner stealer price]
     [:div.ui.label.black @(rf/subscribe [:get-price id_str])]
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
   ; [:div.ui.button.primary {:on-click #(rf/dispatch [:get-contract])} "Init contract"]
   ; [:div.ui.button {:on-click #(rf/dispatch [:get-twittos])} "Get Twittos"]
   [search-bar]
   [results-items]])

(defn trophies-col []
  [:div.column
   [:h1.ui.dividing.header
    "Your Trophies"
    [:span.ui.label.black @(rf/subscribe [:trophies-value])]]
   ; [:div.ui.button {:on-click #(rf/dispatch [:get-trophies])} "Get Trophies"]
   ; (str @(rf/subscribe [:trophies]))
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
