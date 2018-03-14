(ns twittos.db)

(def default-db
  {:web3 js/web3
   :network-id :5777
   :instance nil
   :query ""
   :lol 0})

(def twitter-proxy-url "https://silk-actress.glitch.me/")
(def twitter-search-url (str twitter-proxy-url "search/?page=1&count=5&q="))
(def twitter-lookup-url (str twitter-proxy-url "user/?page=1&count=5&user_id="))
