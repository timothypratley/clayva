(ns clayva.webview
  (:require ["vscode" :as vscode]
            [clayva.extension.db :as db]))

(def default-opts
  {:enableScripts true
   ;; Not sure we need it?
   :retainContextWhenHidden true
   ;; Probably we need it, but get it from the url?
   :portMapping
   ;; TODO: should not hardcode
   [{:webviewPort 1971
     :extensionHostPort 1971}]})

(defn insert-panel! [!state key panel]
  (swap! !state update :webviews assoc key panel))

(defn delete-webview-panel! [!state key]
  (swap! !state update :webviews dissoc key))

(defn ^js select-webview-panel [!state key]
  (get-in @!state [:webviews key]))

(defn set-html! [^js panel title html]
  (doto panel
    (-> .-title (set! title))
    (-> .-webview .-html (set! html))
    (.reveal)))

(defn show! [{:keys [title html key column opts]
                          :or {title "Webview"
                               column vscode/ViewColumn.Beside
                               opts default-opts}}]
  (or (and key
           (when-let [panel (select-webview-panel db/!app-db key)]
             ;; update an existing webview
             (set-html! panel title html)))
      ;; open a new webview
      (let [panel (vscode/window.createWebviewPanel "clayva-webview" title column (clj->js opts))]
        (set-html! panel title html)
        (when key
          (insert-panel! db/!app-db key panel)
          (.onDidDispose panel #(delete-webview-panel! db/!app-db key))))))

(defn url-in-iframe [uri]
  (str "<!DOCTYPE html>
<html>
<head>
<style type=\"text/css\">
  body, html
  {
      margin: 0; padding: 0; height: 100%; overflow: hidden;
  }
  #content
  {
      position:absolute; left: 0; right: 0; bottom: 0; top: 0px;
  }
</style>
</head>
<body style=\"margin:0;padding:0;overflow:hidden;\">
    <iframe src=\"" uri "\" style=\"width:100%; height:100%; border:none;\"></iframe>
</body>
</html>"))
