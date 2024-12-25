(ns clayva.flare-handler
  "A flare is a request for action sent from the REPL server to the REPL client.
   Flares are one-way, from userspace to IDE.
   They are value results of user code that are recognized as special values.
   The REPL client inspects all values it receives, and when it sees a flare, it acts on it.
   Handling a flare may cause the IDE to send an eval request back to the server.
   Flares are IDE-specific, as Calva and Cursive may support different features.
   Flares take the form `{:calva/flare {...}}` where the key indicates the IDE,
   and the value indicates the action."
  (:require ["vscode" :as vscode]
            [clayva.webview :as view]
            [cljs.pprint :as pprint]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [promesa.core :as p]))

(declare eval!)

(defn flare?
  "Value is a flare, intended for the IDE to act upon"
  [value]
  (and (map? value)
       (= (count value) 1)
       (contains? value :calva/flare)))

(defn flare-request
  "Gets the request out of the flare"
  [flare]
  (-> (first flare) (val)))

(defmulti act :type)

(defn inspect [x]
  (when (flare? x)
    (prn 'FLARE)
    (act (flare-request x)))
  x)

(defmethod act :default [request]
  (vscode/window.showErrorMessage (str "Unknown flare request type: " (pr-str (:type request)))))

(defmethod act :info [{:keys [message items then]}]
  (let [p (apply vscode/window.showInformationMessage message (clj->js items))]
    ;; TODO: `then` needs to be a closure to be useful, resolving a symbol is probably not enough
    (when then
      (.then p (fn [selected]
                 (eval! (list (list 'resolve then) selected)))))))

(defmethod act :warn [{:keys [message items then]}]
  (apply vscode/window.showWarningMessage message items))

(defmethod act :error [{:keys [message items then]}]
  (apply vscode/window.showErrorMessage message items))

(defmethod act :webview [{:as request :keys [url loading]}]
  (view/show! (cond-> request
                url (assoc :html (view/url-in-iframe url))
                ;; TODO: make a better script
                loading (update :html str "<script>Loading</script>"))))

;; for now flare inspection is only happening in the Clayva REPL

(def calva
  (-> (vscode/extensions.getExtension "betterthantomorrow.calva") .-exports .-v1))

(defn eval! [code]
  (let [code (if (string? code)
               code
               (pr-str code))
        session js/undefined
        document-ns (or (-> calva .-document (.getNamespace))
                        js/undefined)
        output #js {:stdout #(println "OUT:" %)
                    :stderr #(println "ERR:" %)}]
    (prn 'CODE code)
    (-> (p/let [evaluation (-> calva .-repl (.evaluateCode session code document-ns output))]
          (let [result (.-result evaluation)
                _ (when (str/blank? result)
                    (vscode/window.showErrorMessage "REPL not connected"))
                value (edn/read-string result)]
            (prn 'RESULT value)
            (inspect value)))
        (p/catch (fn [e]
                   (let [ex (ex-info (str "evaluation failed: " e)
                                     {:id ::evaluation-failed
                                      :code code
                                      :session session
                                      :ns document-ns
                                      :error e})]
                     (vscode/window.showErrorMessage (str (ex-message ex) \newline \newline
                                                          (with-out-str (pprint/pprint (ex-data ex)))))))))))