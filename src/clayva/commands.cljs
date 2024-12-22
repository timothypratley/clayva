(ns clayva.commands
  (:require ["vscode" :as vscode]
            [promesa.core :as p]
            [clayva.view :as view]
            ["ext://betterthantomorrow.calva$v1" :as calva]))

(defn send! [code]
  (p/let [evaluation (calva/repl.evaluateCode "clj" code)]
    (vscode/window.showInformationMessage (.-result evaluation))
    (view/show-clay!)))

(defn clay-make-namespace-html-command!+ [_!state s]
  (send!
   '(do
      (println ["Clay make namespace as HTML"
                "$file"])
      (require '[scicloj.clay.v2.api])
      (scicloj.clay.v2.api/make! {:base-source-path nil
                                  :source-path "$file"}))))
