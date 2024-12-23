(ns clayva.commands
  (:require ["vscode" :as vscode]
            [clayva.view :as view]
            [promesa.core :as p]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(def calva
  (-> (vscode/extensions.getExtension "betterthantomorrow.calva") .-exports .-v1))

(defn eval-and-show! [code]
  (let [session (-> calva .-repl (.currentSessionKey))
        ns (-> calva .-document (.getNamespace))
        output #js {:stdout #(println "OUT:" %)
                    :stderr #(println "ERR:" %)}]
    (println "CODE" code)
    (-> (p/let [evaluation (-> calva .-repl (.evaluateCode session code ns output))]
          (let [value (edn/read-string (.-result evaluation))]
            (println "RESULT" value)
            ;; TODO: probably a more explicit way is preferred
            ;; like metadata or tagged literals
            (if (str/starts-with? value "http://")
              (view/show-uri! "Clayva URI" value)
              (view/show! "Clayva HTML" value))))
        (p/catch (fn [e]
                   (throw (ex-info (str "evaluation failed: " e)
                                   {:id ::evaluation-failed
                                    :code code
                                    :session session
                                    :ns ns
                                    :cause e})))))))

;; TODO: maybe clay should have a :single-code configuration? That would preserve formatting
(defn current-form []
  (list 'quote (edn/read-string (second (-> calva .-ranges (.currentForm))))))

;; TODO: find it from the context?
(defn current-file []
  "notebooks/visualization_in_editors.clj")

(def defaults
  {:show false
   :base-source-path nil
   :hide-info-line true
   :hide-ui-header true})

(defn source-path []
  {:source-path (current-file)})

(def uri-defaults
  {})

(defn make-uri
  "Relies on clay to serve the page on localhost"
  [msg config]
  (str (list 'do
             (list 'println msg)
             '(require '[scicloj.clay.v2.api])
             (list 'scicloj.clay.v2.api/make!
                   (merge defaults uri-defaults (source-path) config))
             '(scicloj.clay.v2.api/url))))

(def html-defaults
  {:inline-js-and-css true})

(defn make-html
  "Relies on clay to return html"
  [msg config]
  (str (list 'do
             (list 'println msg)
             '(require '[scicloj.clay.v2.api])
             '(require '[hiccup.core])
             (list '->
                   (list 'scicloj.clay.v2.api/make-hiccup
                         (merge defaults html-defaults (source-path) config))
                   (list 'seq)
                   (list 'hiccup.core/html)))))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn current-form-uri!+ [_!state]
  (-> (make-uri "current form uri"
                {:single-form (current-form)})
      (eval-and-show!)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn current-form-html!+ [_!state]
  (-> (make-html "current form html"
                 {:single-form (current-form)})
      (eval-and-show!)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn namespace-uri!+ [_!state]
  (-> (make-uri "namespace uri"
                {})
      (eval-and-show!)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn namespace-html!+ [_!state]
  (-> (make-html "namespace html"
                 {})
      (eval-and-show!)))

;; TODO: add a show in browser version? but then what to show in the webview?