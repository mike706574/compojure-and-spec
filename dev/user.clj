(ns user
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application."
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.java.javadoc :refer [javadoc]]
   [clojure.pprint :refer [pprint]]
   [clojure.reflect :refer [reflect]]
   [clojure.repl :refer [apropos dir doc find-doc pst source]]
   [clojure.set :as set]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.spec.test.alpha :as stest]
   [clojure.string :as str]
   [clojure.test :as test]
   [clojure.tools.namespace.repl :refer [refresh refresh-all]]
   [clojure.walk :as walk]

   [com.stuartsierra.component :as component]

   [aleph.http :as http]
   [taoensso.timbre :as log]

   [example.client :as client]
   [example.users :as users]
   [example.system :as system]))

(log/set-level! :debug)

(stest/instrument)

(def port 8001)

(def config {:service/id "example-server"
             :service/port port
             :service/base-url (str "http://localhost:" port)
             :service/log-path "/tmp"
             :service/secret-key "secret"
             :example.animals/repo-type "atomic"
             :example.users/manager-type "atomic"
             :service/users {"mike" "rocket"}})

(defonce system nil)

(def url (str "http://localhost:" port))

(defn init
  "Creates and initializes the system under development in the Var
  #'system."
  []
  (alter-var-root #'system (constantly (system/system config)))
  :init)

(defn start
  "Starts the system running, updates the Var #'system."
  []
  (try
    (alter-var-root #'system component/start-system)
    :started
    (catch Exception ex
      (log/error (or (.getCause ex) ex) "Failed to start system.")
      :failed)))

(defn stop
  "Stops the system if it is currently running, updates the Var
  #'system."
  []
  (alter-var-root #'system
                  (fn [s] (when s (component/stop-system s))))
  :stopped)

(defn go
  "Initializes and starts the system running."
  []
  (init)
  (start)
  :ready)

(defn reset
  "Stops the system, reloads modified source files, and restarts it."
  []
  (stop)
  (refresh :after `go))

(defn restart
  "Stops the system, reloads modified source files, and restarts it."
  []
  (stop)
  (go))

  (def client (client/client {:host "localhost:8001"}))

(comment

  @(client/animals client)
  @(client/animals client "ele")
  @(client/add-animal client {:name "elephant"
                             :legs 4
                             :size "huge"})






  )
