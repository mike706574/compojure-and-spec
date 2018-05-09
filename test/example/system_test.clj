(ns example.system-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [com.stuartsierra.component :as component]
            [example.system :as system]
            [example.client :as client]
            [example.macros :refer [with-system unpack-response]]
            [example.util :as util]
            [taoensso.timbre :as log]))

(log/set-level! :trace)

(def port 9001)

(def config {:service/id "example-server"
             :service/port port
             :service/log-path "/tmp"
             :example.users/manager-type "atomic"
             :example.animals/repo-type "atomic"
             :example.users/users {"mike" "rocket"}})

(deftest animals
  (with-system (system/system config)
    (let [client (-> {:host (str "localhost:" port)}
                     (client/client))]
      (unpack-response @(client/animals client)
        (is (= 200 status))
        (is (= {:status "ok" :animals []} body)))
      (unpack-response @(client/animals client "do")
        (is (= 200 status))
        (is (= {:status "ok" :animals []} body)))
      (unpack-response @(client/add-animal client {:name "elephant"
                                                   :legs 4
                                                   :size "huge"})
        (is (= 201 status))
        (is (= {:status "added"
                :animal {:name "elephant",
                         :size "huge",
                         :legs 4
                         :id "1"}}
               body)))
      (unpack-response @(client/animals client)
        (is (= 200 status))
        (is (= {:status "ok"
                :animals [{:name "elephant",
                           :size "huge",
                           :legs 4
                           :id "1"}]}
               body)))
      )))

(deftest greeting
  (with-system (system/system config)
    (let [client (-> {:host (str "localhost:" port)}
                     (client/client)
                     (client/authenticate {:username "mike"
                                           :password "rocket"}))]
      (unpack-response @(client/greeting client "mike")
        (is (= 200 status))
        (is (= {:greeting "Hello, mike!"} body))))))

(deftest authenticates-successfully
  (with-system (system/system config)
    (let [client (-> {:host (str "localhost:" port)}
                     (client/client)
                     (client/authenticate {:username "mike"
                                           :password "rocket"}))
          token (:token client)]
      (is (not (str/blank? token))))))

(deftest fails-to-authenticate
  (with-system (system/system config)
    (let [client (-> {:host (str "localhost:" port)}
                     (client/client)
                     (client/authenticate {:username "mike"
                                           :password "kablam"}))
          token (:token client)]
      (is (nil? token)))))
