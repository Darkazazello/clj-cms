(ns clj-cms.cljs.tree
  (:require [enfocus.core :as ef])
  (:require-macros [enfocus.macros :as em]))

(defn start []
  (ef/at "body" (ef/content "Hello world!")))
(defn ss [] "addss")

;; (set! (.-onload js/window) start)
(set! (.-onload js/window) #(em/wait-for-load (start)))

