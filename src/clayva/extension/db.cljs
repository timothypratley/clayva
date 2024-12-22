(ns clayva.extension.db)

(def init-db {:extension/context nil
              :extension/disposables []
              :extension/when-contexts {:clayva/active? false}})

(defonce !app-db (atom init-db))

(comment
  @!app-db
  :rcf)

