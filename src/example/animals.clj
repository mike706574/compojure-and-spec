(ns example.animals
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(s/def ::id string?)
(s/def ::name string?)
(s/def ::legs pos-int?)
(s/def ::size #{"tiny" "small" "medium" "large" "huge" "gigantic"})

(s/def ::animal-template (s/keys :req-un [::name
                                          ::legs
                                          ::size]))

(s/def ::animal (s/merge ::animal-template
                         (s/keys :req-un [::id])))
(s/def ::animals (s/coll-of ::entry))

(defprotocol AnimalRepo
  "Stores information about animals."
  (animals [this] [this name] "Retrieves animals.")
  (add-animal [this animal] "Adds an animal.")
  (delete-animal [this id] "Deletes an animal."))

(s/def ::animal-repo (partial satisfies? AnimalRepo))

(defmulti repo ::repo-type)

(defmethod repo :default
  [{type ::repo-type}]
  (throw (ex-info (str "Invalid animal repo type: " type)
                  {:animal-repo-type type})))

(s/def ::repo-type #{"atomic"})
