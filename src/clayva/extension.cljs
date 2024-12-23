(ns clayva.extension
  (:require [clayva.commands]
            [clayva.extension.db :as db]
            [clayva.extension.life-cycle-helpers :as lc-helpers]
            [clayva.extension.when-contexts :as when-contexts]
            [clojure.string :as str]))

;;;;; Extension activation entry point

(defn ^:export activate [context]
  (js/console.time "activation")
  (js/console.timeLog "activation" "Clayva activate START")
  (when context
    (swap! db/!app-db assoc :extension/context context))
  (doseq [[sym the-var] (ns-publics 'clayva.commands)
          :let [s (str sym)]
          :when (str/ends-with? s "!+")]
    (lc-helpers/register-command! db/!app-db
                                  (str "clayva." (subs s 0 (- (count s) 2)))
                                  the-var))
  (when-contexts/set-context!+ db/!app-db :clayva/active? true)

  (js/console.timeLog "activation" "Clayva activate END")
  (js/console.timeEnd "activation")
  #js {:v1 {}})

(comment
  ;; When you have updated the activate function, cleanup and call activate again
  ;; NB: If you have updated the extension manifest, you will need to restart the extension host instead
  (lc-helpers/cleanup! db/!app-db)
  (activate nil)
  :rcf)

;;;;; Extension deactivation entry point

(defn ^:export deactivate []
  (lc-helpers/cleanup! db/!app-db))


;;;;; shadow-cljs hot reload hooks
;; We don't need to do anything here, but it is nice to see that reloading is happening

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn ^{:dev/before-load true} before-load []
  (println "shadow-cljs reloading..."))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn ^{:dev/after-load true} after-load []
  (println "shadow-cljs reload complete"))
