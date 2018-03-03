(ns twittos.main
  (:require [reagent.core :as reagent]
            [twittos.ahah :refer [ahah]]))

(defn ^:export mount []
  (js/console.log "INIT")
  (reagent/render [ahah] (js/document.getElementById "app")))
