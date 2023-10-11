(ns sample.app.web.models.utils
  (:require
    [clojure.spec.alpha :as spec]
    [clojure.string :as string]
    [hugsql.parameters :refer [identifier-param-quote]]
    [integrant.repl.state :as state]))

(defn spec-error
  [spec input]
  (ex-info (spec/explain-str spec input)
           {:type :system.exception/business}))

(defn db-error
  [error]
  (ex-info (or (ex-message error) error)
           (merge (ex-data error)
                  {:type :system.exception/business})))

(defn validate-spec
  [spec input]
  (if (spec/valid? spec input)
    input
    (throw (spec-error spec input))))

(defn query-fn [& vars]
  (try
    (apply (:db.sql/query-fn state/system) vars)
    (catch Exception e (throw (db-error e)))))

(defn transpile-query
  [params command separator options]
    (clojure.string/join " AND "
      (for [[field value] (get params command)]
        (let [expression (or (:raw value) (str " = :v" command "."))]
          (str (identifier-param-quote (name field) options)
            expression (name field))))))

(defn expand-set
  [params options]
    (transpile-query params :set ", " options))

(defn expand-where
  [params options]
  (if (= params {:where :all})
    " id IS NOT NULL "
    (transpile-query params :where " AND " options)))