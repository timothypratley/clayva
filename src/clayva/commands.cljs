(ns clayva.commands
  (:require ["vscode" :as vscode]
            [clayva.view :as view]
            [clojure.string :as str]
            [promesa.core :as p]
            [clojure.edn :as edn]))

(def calva
  (-> (vscode/extensions.getExtension "betterthantomorrow.calva") .-exports .-v1))

(prn (js-keys (-> calva .-repl)))

(defn send! [code]
  (let [session (-> calva .-repl (.currentSessionKey))
        ns (-> calva .-document (.getNamespace))
        output #js {:stdout #(println "OUT:" %)
                    :stderr #(println "ERR:" %)}]
    (-> (p/let [evaluation (-> calva .-repl (.evaluateCode session code ns output))]
          ;;(def evaluation evaluation)
          (view/show! "Clayva" (edn/read-string (.-result evaluation))))
        (p/catch (fn [e]
                   (throw (ex-info (str "evaluation failed: " e)
                                   {:id ::evaluation-failed
                                    :code code
                                    :session session
                                    :ns ns
                                    :cause e})))))))


(comment
  calva
  (require '[clojure.edn :as edn])
  (edn/read-string (.-result evaluation))
  (js->clj evaluation))



(defn clay-make-namespace-html-command!+ [_!state s]
  (send! (str (list 'do
                    '(println ["Clay render current form"])
                    '(require '[scicloj.clay.v2.api])
                    ;;'(require '[scicloj.kindly.v4.kind :as kind])
                    '(require '[hiccup.core :as hiccup])
                    (list
                     '->
                     (list 'scicloj.clay.v2.api/make-hiccup
                           {:show false
                            :base-source-path nil
                            :single-form (list 'quote (edn/read-string (second (-> calva .-ranges (.currentForm)))))
             ;; :source-path currentfile
                            :inline-js-and-css true
                            })
                     (list 'seq)
                     (list 'hiccup/html))))))

(comment
  (+ 1 2)
  {:a [1 2 3]
   :b ["hahahaha" "hohohoho"]})

#_(defn clay-make-namespace-html-command!+ [_!state s]
  (info "S:" s)
  (send!
   (list 'do
         '(println ["Clay make namespace as HTML"])
         '(require '[scicloj.clay.v2.api])
         (list 'scicloj.clay.v2.api/make!
               {:base-source-path nil
                :source-path (-> calva .-ranges (.getCurrentForm))}))))
