(ns twittos.db)

(def default-db
  {:web3 js/web3
   :network-id :5777
   :instance nil
   :query ""
   :lol 0})

(def twitter-proxy-url "https://silk-actress.glitch.me/search/")
(def twitter-search-url (str twitter-proxy-url "?page=1&count=5&q="))
