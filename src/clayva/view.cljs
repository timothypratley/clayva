(ns clayva.view
  (:require ["vscode" :as vscode]))

(defonce kindly-view*
  (atom nil))

(defn ^js open-view!
  "Returns the view if it exists, otherwise creates one"
  []
  (or (some-> @kindly-view* (doto #(.reveal ^js %)))
      (doto (vscode/window.createWebviewPanel "kindly-render"
                                              "Kindly render"
                                              vscode/ViewColumn.One
                                              #js{:enableScripts true})
        (.onDidDispose #(reset! kindly-view* nil))
        (.reveal)
        (->> (reset! kindly-view*)))))

(defn show!
  [title html]
  (when-let [view (open-view!)]
    (set! (-> view .-title) title)
    (set! (-> view .-webview .-html) html)))

(defn show-uri!
  [title uri]
  (show! title
         (str "<html>
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
</html>")))
