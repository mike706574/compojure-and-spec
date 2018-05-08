(ns example.routes
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [compojure.api.sweet :as compojure]
            [example.animals :as animals]
            [example.authentication :as auth]
            [example.users :as users]
            [taoensso.timbre :as log]
            [ring.util.http-response :as response]))

(s/def ::name string?)
(s/def ::greeting string?)

(s/def ::status #{"ok" "added" "deleted" "error"})

(s/def ::animal-response (s/keys :req-un [::status ::animals/animal]))
(s/def ::animals-response (s/keys :req-un [::status ::animals/animals]))

(defn to-http [response]
  (log/debug "Response:" response)
  (let [f (case (:status response)
            "ok" response/ok
            "deleted" response/ok
            "error" response/internal-server-error)]
    (f response)))

(defn routes [{:keys [authenticator user-manager animal-repo base-url]}]
  (let [animal-url (str base-url "/api/animals")
        authentication-handler (fn [handler]
                                 (fn [request]
                                   (if (auth/authenticated? authenticator request)
                                     (handler request)
                                     {:status 401})))]
    (compojure/context "/api" []
      (compojure/context "/greetings" []
        :tags ["greetings"]
        :middleware [authentication-handler]
        (compojure/resource
         {:get
          {:summary "retrieving friendly greetings"
           :parameters {:query-params (s/keys :req-un [::name])}
           :responses {200 {:schema (s/keys :req-un [::greeting])}}
           :handler (fn [{{:keys [name]} :query-params}]
                      (log/info (str "Saying hello to " name "."))
                      (response/ok {:greeting (str "Hello, " name "!")}))}}))

      (compojure/context "/animals" []
        :tags ["animals"]
        (compojure/resource
         {:get
          {:summary "retrieving animals"
           :parameters {:query-params (s/keys :opt-un [::name])}
           :responses {200 {:schema ::animals-response}}
           :handler (fn [{{name :name} :query-params}]
                      (to-http (if name
                                 (animals/animals animal-repo name)
                                 (animals/animals animal-repo))))}
          :post
          {:summary "adding an animal"
           :parameters {:body-params ::animals/animal-template}
           :responses {200 {:schema ::animal-response}}
           :handler (fn [{animal :body-params}]
                      (let [{status :status :as response} (animals/add-animal animal-repo animal)]
                        (if (= status "added")
                          (response/created animal-url response)
                          (to-http response))))}
          :delete
          {:summary "deleting an animal"
           :parameters {:query-params (s/keys :req-un [::animals/id])}
           :responses {200 {:schema ::animal-response}}
           :handler (fn [{{id :id} :query-params}]
                      (to-http (animals/delete-animal animal-repo id)))}}))

      (compojure/context "/tokens" []
        :tags ["tokens"]
        (compojure/resource
         {:post
          {:summary "creating tokens"
           :parameters {:body-params ::users/credentials}
           :responses {201 {:schema (s/keys :req-un [::users/token])}
                       401 {:schema nil?}}
           :handler (fn [{credentials :body-params}]
                      (log/info (str "Creating token for " (:username credentials) "."))
                      (if-let [user (users/authenticate user-manager credentials)]
                        (let [token (auth/token authenticator (:username credentials))]
                          (response/created "n/a" {:token token}))
                        (response/unauthorized)))}})))))
