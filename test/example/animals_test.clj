(ns example.animals-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [com.stuartsierra.component :as component]
            [example.animals :as animals]
            [example.animals.atomic :as atomic]
            [example.util :as util]
            [taoensso.timbre :as log]))

(log/set-level! :trace)

(def port 9001)

(def config {:service/id "example-server"
             :service/port port
             :service/log-path "/tmp"
             :example.users/manager-type "atomic"
             :example.animals/repo-type "atomic"
             :service/users {"mike" "rocket"}})

(def repo (animals/repo {::animals/repo-type "atomic"}))

(animals/animals repo)
(animals/add-animal repo {:name "elephant"
                          :legs 4
                          :size "huge"})


(defn exercise-repo [repo]
  (is (= {:status "ok" :animals []}
         (animals/animals repo)))

  (is (= {:status "added", :animal {:name "elephant",
                                    :legs 4,
                                    :size "huge",
                                    :id "1"}}
         (animals/add-animal repo {:name "elephant"
                                   :legs 4
                                   :size "huge"})))

  (is (= {:status "ok" :animals [{:name "elephant",
                                  :legs 4,
                                  :size "huge",
                                  :id "1"}]}
         (animals/animals repo)))

  (is (= {:status "ok" :animals [{:name "elephant",
                                  :legs 4,
                                  :size "huge",
                                  :id "1"}]}
         (animals/animals repo "pha")))

  (is (= {:status "ok" :animals []}
         (animals/animals repo "do")))

  (is (= {:status "deleted" :animal {:name "elephant",
                                     :legs 4,
                                     :size "huge",
                                     :id "1"}}
         (animals/delete-animal repo "1")))

  (is (= {:status "ok" :animals []}
         (animals/animals repo))))

(deftest atomic
  (exercise-repo (animals/repo {::animals/repo-type "atomic"})))
