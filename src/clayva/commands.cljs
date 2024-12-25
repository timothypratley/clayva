(ns clayva.commands
  (:require
   ["vscode" :as vscode]
   [clayva.flare-handler :as flare-handler]
   [clojure.edn :as edn]))

(def calva
  (-> (vscode/extensions.getExtension "betterthantomorrow.calva") .-exports .-v1))

;; TODO: maybe clay should have a :single-code configuration? That would preserve formatting
(defn current-form []
  (list 'quote (edn/read-string (second (-> calva .-ranges (.currentForm))))))

(defn current-file []
  (some-> vscode/window.activeTextEditor .-document .-uri .-path))

(def defaults
  {:show true
   :browse false
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
  (list 'do
        (list 'println msg)
        '(require '[scicloj.clay.v2.api])
        (list 'scicloj.clay.v2.api/make!
              (merge defaults uri-defaults (source-path) config))
        '(->> (scicloj.clay.v2.api/url)
              (hash-map :type :webview :key :clay :title "Clay" :url)
              (hash-map :calva/flare))))

(def html-defaults
  {:inline-js-and-css true})

(defn make-html
  "Relies on clay to return html"
  [msg config]
  (list 'do
        (list 'println msg)
        '(require '[scicloj.clay.v2.api])
        '(require '[hiccup.core])
        (list '->
              (list 'scicloj.clay.v2.api/make-hiccup
                    (merge defaults html-defaults (source-path) config))
              (list 'seq)
              (list 'hiccup.core/html)
              '(->> (hash-map :type :webview :key :clay :title "Clay" :html)
                    (hash-map :calva/flare)))))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn current-form-uri!+ [_!state]
  (-> (make-uri "current form uri"
                {:single-form (current-form)})
      (flare-handler/eval!)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn current-form-html!+ [_!state]
  (-> (make-html "current form html"
                 {:single-form (current-form)})
      (flare-handler/eval!)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn namespace-uri!+ [_!state]
  (-> (make-uri "namespace uri"
                {})
      (flare-handler/eval!)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn namespace-html!+ [_!state]
  (-> (make-html "namespace html"
                 {})
      (flare-handler/eval!)))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn eval-current-form!+ [_!state]
  (flare-handler/eval! (current-form)))
