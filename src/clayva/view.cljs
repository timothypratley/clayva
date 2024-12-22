(ns clayva.view
  ["vscode" :as vscode])

(defonce kindly-view*
  (atom nil))

(defn open-view!
  "Returns the view if it exists, otherwise creates one"
  []
  (or (some-> @kindly-view* (doto (.reveal)))
      (doto (vscode/window.createWebviewPanel "kindly-render"
                                              "Kindly render"
                                              vscode/ViewColumn.One
                                              #js{:enableScripts true})
        (.onDidDispose #(reset! kindly-view* nil))
        (->> (reset! kindly-view*)))))

(defn show!
  [title html]
  (when-let [view (open-view!)]
    (set! (-> view .-title) title)
    (set! (-> view .-webview .-html) html)))

(defn show-clay!
  []
  (show-html!
   "Clayva"
   "<html>
<head></head>
<body style=\"margin:0;padding:0;overflow:hidden;\">
  <iframe src=\"http://localhost:1971\" style=\"width:100%; height:100%; border:none;\"></iframe>
</body>
</html>"))
