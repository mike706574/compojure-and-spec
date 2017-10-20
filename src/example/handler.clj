(ns example.handler
  (:require [clojure.string :as str]
            [com.stuartsierra.component :as component]
            [compojure.api.sweet :as compojure]
            [example.routes :as routes]
            [example.util :as util]
            [ring.middleware.defaults :refer [wrap-defaults
                                              api-defaults]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.cors :refer [wrap-cors]]
            [taoensso.timbre :as log]))

(defn api-handler [deps]
  (compojure/api
   {:coercion :spec
    :exceptions {:handlers {:compojure.api.exception/default (fn [x]
                                                               (println x))}}
    :swagger {:ui "/api/docs"
              :spec "/api/swagger.json"
              :data {:info {:title "greeting"
                            :description "an api"}
                     :tags [{:name "hello", :description "Hello"}]}}}
   (routes/routes deps)))

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

(defrecord ExampleHandlerFactory []
  HandlerFactory
  (handler [this]
    (wrap-logging (api-handler this))))

(defn factory
  [config]
  (component/using
   (ExampleHandlerFactory.)
   [:authenticator :user-manager]))
