(ns example.users
  (:require [buddy.hashers :as hashers]
            [clojure.spec.alpha :as s]))

(s/def ::username string?)
(s/def ::password string?)
(s/def ::token string?)
(s/def ::credentials (s/keys :req-un [::username ::password]))

(defprotocol UserManager
  "Abstraction around user storage and authentication."
  (add! [this user] "Adds a user.")
  (authenticate [this credentials] "Authenticates a user."))

(s/def ::manager (partial satisfies? UserManager))

(s/fdef add!
  :args (s/cat :user-manager ::manager
               :credentials ::credentials)
  :ret ::credentials)

(defmulti user-manager ::manager-type)

(defn ^:private find-by-username
  [users username]
  (when-let [user (first (filter (fn [[user-id user]] (= (:username user) username)) @users))]
    (val user)))

(defrecord AtomicUserManager [counter users]
  UserManager
  (add! [this user]
    (swap! users assoc (str (swap! counter inc))
           (update user :password hashers/encrypt))
    (dissoc user :password))

  (authenticate [this {:keys [username password]}]
    (when-let [user (find-by-username users username)]
      (when (hashers/check password (:password user))
        (dissoc user :password)))))

(defmethod user-manager "atomic"
  [config]
  (let [user-manager (AtomicUserManager. (atom 0) (atom {}))]
    (when-let [users (::users config)]
      (doseq [[username password] users]
        (add! user-manager {:username username
                            :password password})))
    user-manager))

(defmethod user-manager :default
  [{type ::manager-type :as config}]
  (throw (ex-info (str "Invalid user manager type: " type)
                  {:user-manager-type type})))

(s/def ::manager-type #{"atomic"})
