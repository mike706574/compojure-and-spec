(ns example.client
  (:require [aleph.http :as http]
            [example.users :as users]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defn parse
  [response]
  (if-let [body (:body response)]
    (let [content-type (get-in response [:headers "content-type"])
          json? (and content-type (str/starts-with? content-type "application/json"))]
      (assoc response :body (if json?
                              (json/read (io/reader body) :key-fn keyword)
                              (slurp body))))
    response))

(defn add-user!
  [system username password]
  (users/add! (:user-manager system) {:service/username username
                                      :service/password password}))

(defn http-url [host] (str "http://" host))

(defn get-token
  [host credentials]
  (let [response (parse @(http/post (str (http-url host) "/api/tokens")
                                    {:headers {"Content-Type" "application/json"
                                               "Accept" "text/plain"}
                                     :body (json/write-str credentials)
                                     :throw-exceptions false}))]
    (case (:status response)
      201 (-> response :body :token)
      401 nil
      (throw (ex-info "Failed to fetch token." {:username (:username credentials)
                                                :response response})))))

(defprotocol Client
  (authenticate [this credentials])
  (greeting [this name]))

(defrecord ServiceClient [host token]
  Client
  (authenticate [this credentials]
    (when-let [token (get-token host credentials)]
      (assoc this :token token)))

  (greeting [this name]
   (parse @(http/get (str (http-url host) (str "/api/greetings"))
                     {:headers {"Content-Type" "application/json"
                                "Accept" "application/json"}
                      :query-params {"name" name}
                      :throw-exceptions false}))))

(defn client
  [{:keys [host content-type]}]
  (map->ServiceClient {:host host
                       :content-type content-type}))
