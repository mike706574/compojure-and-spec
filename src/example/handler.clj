(ns example.handler
  (:require [clojure.string :as str]
            [com.stuartsierra.component :as component]
            [compojure.api.exception :as ex]
            [compojure.api.sweet :as compojure]
            [compojure.api.upload :as upload]
            [example.routes :as routes]
            [example.util :as util]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.params :refer [wrap-params]]
            [taoensso.timbre :as log]))

(defn api-handler [deps]
  (-> {:coercion :spec
       :exceptions {:handlers {:compojure.api.exception/default
                               (fn [error]
                                 (log/error error "Exception thrown by compojure-api.")
                                 {:status 500
                                  :content-type "text/plain"
                                  :body "An error occurred. Please see the logs for details."})}}
       :swagger {:ui "/api/docs"
                 :spec "/api/swagger.json"
                 :data {:info {:title "example"
                               :description "an api"}
                        :tags [{:name "example", :description "example"}]}}}
      (compojure/api (routes/routes deps))
      (upload/wrap-multipart-params)
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])))

(defn wrap-logging
  [handler]
  (fn [{:keys [uri request-method] :as request}]
    (let [label (str (-> request-method name str/upper-case) " \"" uri "\"")]
      (try
        (log/debug label)
        (let [{:keys [status] :as response} (handler request)]
          (log/debug (str label " -> " status))
          (log/trace "Full response:\n" (util/pretty response))
          response)
        (catch Exception e
          (log/error e label)
          {:status 500})))))

(defprotocol HandlerFactory
  "Builds a request handler."
  (handler [this]))

(defrecord ExampleHandlerFactory [animal-repo
                                  authenticator
                                  base-url
                                  user-manager]
  HandlerFactory
  (handler [this]
    (wrap-logging (api-handler this))))

(defn factory
  [{:keys [:service/base-url]}]
  (component/using
   (map->ExampleHandlerFactory {:base-url base-url})
   [:animal-repo :authenticator :user-manager]))
