(ns action
  (:require [nbb.core :as nbb]
            [foo]))

(defn action
  ;; deps passed by index.mjs
  [deps]
  (prn :CLI foo/bar)
  (let [{:strs [actionsCore actionsGithub]} (js->clj deps)]
    (try
      (let [name-to-greet (.getInput actionsCore "who-to-greet")
            _ (.log js/console (str "Hello " name-to-greet " with version " (nbb/version) "!"))
            time (.toTimeString (js/Date.))
            _ (.setOutput actionsCore "time" time)
            payload (.. actionsGithub -context -payload)
            payload (js/JSON.stringify payload nil 2)
            _ (.log js/console (str "The event payload: " payload))])
      (catch :default e
        (.setFailed actionsCore (.-message e))))))

;; exports:
#js {:action action}
