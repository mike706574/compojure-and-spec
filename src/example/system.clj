(ns example.system
  (:require [example.authentication :as auth]
            [example.users :as users]
            [example.handler :as handler]
            [example.service :as service]
            [example.util :as util]
            [clojure.spec.alpha :as s]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]
            [taoensso.timbre.appenders.core :as appenders]))

(defn configure-logging!
  [{:keys [:service/id :service/log-path] :as config}]
  (let [log-file (str log-path "/" id "-" (util/uuid))]
    (log/merge-config!
     {:appenders {:spit (appenders/spit-appender
                         {:fname log-file})}})))

(s/def :service/id string?)
(s/def :service/port integer?)
(s/def :service/log-path string?)
(s/def :service/user-manager-type #{:atomic})
(s/def :service/users (s/map-of :service/username :service/password))

(s/def :service/config (s/keys :req [:service/id
                                     :service/port
                                     :service/log-path
                                     :service/user-manager-type]
                               :opt [:service/users]))

(defn ^:private build
  [config]
  (log/info (str "Building " (:service/id config) "."))
  (configure-logging! config)
  {:authenticator (auth/authenticator config)
   :user-manager (users/user-manager config)
   :handler-factory (handler/factory config)
   :app (service/aleph-service config)})

(defn system
  [config]
  (if-let [validation-failure (s/explain-data :service/config config)]
    (do (log/error (str "Invalid configuration:\n"
                        (util/pretty config)
                        "Validation failure:\n"
                        (util/pretty validation-failure)))
        (throw (ex-info "Invalid configuration." {:config config
                                                  :validation-failure validation-failure})))
    (build config)))

(s/fdef system
  :args (s/cat :config :service/config)
  :ret map?)
