(ns twittos.views
  (:require [re-frame.core :as rf]
            [clojure.string :as str]
            [cljs-web3.core :as web3-core]
            [twittos.owner :refer [owner-only]]
            [twittos.timer :refer [clock]]))

(defn ->normal [img-url] (some-> img-url (str/replace "_normal" "")))

(defn search-bar []
  [:div.ui.massive.fluid.input.action.mt1
   [:input {:type "text" :placeholder "Search for Twittos"
            :value @(rf/subscribe [:get :query])
            :on-key-down #(if (#{"Enter"} (.-key %)) (rf/dispatch [:search-twitter]))
            :on-change #(rf/dispatch [:set :query (.. % -target -value)])}]
   [:button.ui.icon.button.orange
    {:on-click #(rf/dispatch [:search-twitter])
     :class (if @(rf/subscribe [:get :searching?]) "loading")}
    [:i.icon.search]]])

(defn results-cards []
  [:div.ui.cards
   (for [{:keys [id_str name screen_name description profile_image_url_https]}
         @(rf/subscribe [:get :results])
         :let [image-url (->normal profile_image_url_https)]
         :when (some? id_str)]
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
    [:a.image {:href (str "//twitter.com/" screen_name) :target "_blank"}
     [:div.ui.right.ribbon.label.green @(rf/subscribe [:get-price id_str])]
     [:img {:src (->normal profile_image_url_https)}]]
    [:div.content
     [:div.header name]
     [:div.meta.orange-text "@" screen_name]]]])

(defn twitto-item' [{:keys [id_str name screen_name description profile_image_url_https owner stealer price block stealable?]}]
  [:div.item
   [:a.ui.image.tiny
    {:href (str "//twitter.com/" screen_name) :target "_blank"}
    [:img {:src (->normal profile_image_url_https)}]]
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
   (for [{:keys [id_str] :as result} @(rf/subscribe [:results])
         :when (some? id_str)]
     ^{:key id_str}
     [twitto-item' result])])

(defn search-col []
  [:div.column
   [:h2.ui.dividing.header
    [:img.ui.image.logo {:src "img/twittos.png"}]
    "Crypto" [:span.orange-text "Twittos"]
    [:div.ui.sub.header "Steal 'Em All"]]
   [:h4 "Steal virtual ownership of Twitter accounts and set the price someone has to pay to steal them back from you. First steals are free. Game on!"]
   (if (nil? @(rf/subscribe [:get :web3]))
     [:div.ui.massive.orange.message
      [:div.header "Loading connection to an Ethereum node..."]
      [:p "Connect to " [:strong "Portis"] "." [:br] "Or get "
       [:a {:href "https://metamask.io/" :target "_blank"} "MetaMask"]
       " if you're on desktop — "
       [:a {:href "https://wallet.coinbase.com/" :target "_blank"} "Coinbase Wallet"]
       " if you're on mobile."]]
     [search-bar])
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
   [:div.ui.cards.two.column.relaxed.stackable.grid
    (if-let [trophies @(rf/subscribe [:trophies])]
      (for [{:keys [id_str] :as trophy} trophies
            :when (some? id_str)]
        ^{:key id_str}
        [twitto-item trophy])
      [:div.mt1 "The Twittos you steal will appear here."])]])

(defn steals-col []
  [:div.column
   [:h2.ui.dividing.header
    "Live Steals"
    [:div.ui.sub.header
     [clock]]]
   (if (empty? @(rf/subscribe [:get :steals]))
     [:div.mt1 "Loading last stolen Twittos..."]
     [:div.ui.divided.items
      (for [{:keys [id_str new-price] :as stolen-twitto} @(rf/subscribe [:stolen-twittos])
            :when (some? id_str)]
        ^{:key (str id_str new-price)}
        [twitto-item' stolen-twitto])])])

(defn main []
  [:div.ui.stackable.three.column.relaxed.grid
   [owner-only]
   [:div.row
    [search-col]
    [trophies-col]
    [steals-col]]])
