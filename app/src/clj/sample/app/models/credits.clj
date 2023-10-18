(ns sample.app.models.credits
  (:require
    [clojure.spec.alpha :as spec]
    [sample.app.utils :as utils]))

(spec/def :credits/id (spec/and string? #(re-matches utils/uuid-regex %)))
(spec/def :credits/user_id :users/uuid)
(spec/def :credits/value #(and (number? %) (pos? %)))

(spec/def :credits/add-credit!
  (spec/keys :req-un [:credits/user_id
                      :credits/value]))

(def select-fields
  [[:credits.created_at :created_at]
   [:credits.id :id]
   [:credits.user_id :user_id]
   [:credits.value :value]
   [:users.email :user_email]
   [:users.status :user_status]])

(defn add-credit!
  "Add given amount. Returns 1 on success."
  [user-id amount]
  (->> {:user_id user-id :value amount}
    (utils/validate-spec :credits/add-credit!)
    (#(utils/hsql-execute! {:insert-into :credits
                            :columns     [:user_id :value]
                            :values      [[(:user_id %) (:value %)]]}))
    (utils/format-hsql-output)))

(defn get-credits
  "Retrieve credits based on the 'where' conditions. Returns a coll of retrieved credits."
  ([]
  (get-credits nil))
  ([where]
  (->> where
    (conj [:and [:<> :credits.deleted true]])
    (utils/validate-spec :general/query)
    (#(utils/hsql-execute! {:select    select-fields
                            :from      [:credits]
                            :join      [:users [:= :credits.user_id :users.id]]
                            :where     %
                            :order-by  [:credits.id]})))))

(defn get-deleted-credits
  "Retrieve deleted credits based on the 'where' conditions. Returns a coll of retrieved credits."
  ([]
  (get-credits nil))
  ([where]
  (->> where
    (#(vec (conj [:and [:= :credits.deleted true]] %)))
    (utils/validate-spec :general/query)
    (#(utils/hsql-execute! {:select    (conj select-fields [:credits.deleted :deleted])
                            :from      [:credits]
                            :join      [:users [:= :credits.user_id :users.id]]
                            :where     %
                            :order-by  [:credits.id]})))))

(defn delete-credit!
  "Delete a credit identified by its ID. Returns 1 on success."
  [id]
  (->> id
    (utils/validate-spec :credits/id)
    (#(utils/hsql-execute! {:update :credits
                            :set    {:deleted true}
                            :where  [:= :id %]}))
    (utils/format-hsql-output)))
