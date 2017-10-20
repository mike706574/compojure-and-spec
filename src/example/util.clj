(ns example.util)

(defn pretty
  [form]
  (with-out-str (clojure.pprint/pprint form)))

(defn uuid [] (str (java.util.UUID/randomUUID)))
