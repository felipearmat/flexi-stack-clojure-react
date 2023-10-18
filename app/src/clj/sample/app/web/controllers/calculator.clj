(ns sample.app.web.controllers.calculator
  (:require
    [ring.util.http-response :as http-response]
    [sample.app.web.services.calculator :as calculator]))

(defn calculate
  [req]
  (let [user (:user (:identity req))
        expression (:expression (:params req))]
        (http-response/ok (calculator/calculate! expression))))