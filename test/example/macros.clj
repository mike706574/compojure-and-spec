(ns example.macros
  (:require [example.util :as util]
            [com.stuartsierra.component :as component]))

(defmacro with-system
  [system-map & body]
  `(let [~'system (component/start-system ~system-map)]
     (try
       ~@body
       (finally (component/stop-system ~'system)))))

(defmacro unpack-response
  [call & body]
  `(let [~'response ~call
         ~'status (:status ~'response)
         ~'body (:body ~'response)
         ~'headers (:headers ~'response)
         ~'text (util/pretty ~'response)]
     ~@body))
