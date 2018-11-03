(ns twittos.db)

;; TODO Drop a spec in here

(def default-db
  (merge {:instance nil
          :owner ""
          :query ""
          :stolen-trophies #{}
          :steals '()}))

(def twitter-proxy-url "https://silk-actress.glitch.me/")
(def twitter-search-url (str twitter-proxy-url "search/?page=1&count=5&q="))
(def twitter-lookup-url (str twitter-proxy-url "user/?page=1&count=25&user_id="))

(def etherscan-lookup-url "https://silk-actress.glitch.me/etherscan/")
