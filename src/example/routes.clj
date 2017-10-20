(ns example.routes
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [compojure.api.sweet :as compojure]
            [example.authentication :as auth]
            [example.users :as users]
            [taoensso.timbre :as log]
            [ring.util.http-response :as response]))

(s/def ::name string?)
(s/def ::greeting string?)

(s/def ::username string?)
(s/def ::password string?)
(s/def ::credentials (s/keys :req-un [::username
                                      ::password]))

(s/def ::token string?)

(defn routes [{:keys [authenticator user-manager]}]
  (compojure/context "/api" []
    (compojure/context "/greetings" []
      :tags ["greetings"]
      (compojure/resource
       {:get
        {:summary "retrieving friendly greetings"
         :parameters {:query-params (s/keys :req-un [::name])}
         :responses {200 {:schema (s/keys :req-un [::greeting])}}
         :handler (fn [{{:keys [name]} :query-params}]
                    (log/info (str "Saying hello to " name "."))
                    (response/ok {:greeting (str "Hello, " name "!")}))}}))

    (compojure/context "/tokens" []
      :tags ["tokens"]
      (compojure/resource
       {:post
        {:summary "creating tokens"
         :parameters {:body-params ::credentials}
         :responses {201 {:schema (s/keys :req-un [::token])}
                     401 {:schema nil?}}
         :handler (fn [{credentials :body-params}]
                    (log/info (str "Creating token for " (:username credentials) "."))
                    (if-let [user (users/authenticate user-manager credentials)]
                      (let [token (auth/token authenticator (:username credentials))]
                        (response/created "n/a" {:token token}))
                      (response/unauthorized)))}}))))
