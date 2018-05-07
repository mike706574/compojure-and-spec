(ns example.animals.atomic
  (:require [clojure.string :as str]
            [example.animals :refer [repo]])
  (:import [example.animals AnimalRepo]))

(defn search-map [m k v]
  (into [] (filter (comp #(str/includes? % v) k) (vals m))))

(defrecord AtomicAnimalRepo [counter animals]
  AnimalRepo
  (animals [this]
    (println "animals" @animals)
    {:status "ok" :animals (or (vals @animals) [])})

  (animals [this term]
    (println "animals by name" @animals term)
    {:status "ok" :animals (search-map @animals :name term)})

  (add-animal [this animal]
    (let [id (str (swap! counter inc))
          animal (assoc animal :id id)]
      (swap! animals assoc id animal)
      {:status "added" :animal animal}))

  (delete-animal [this id]
    (let [animal (get @animals id)]
      (swap! animals dissoc id)
      {:status "deleted" :animal animal})))

(defmethod repo "atomic"
  [config]
  (map->AtomicAnimalRepo {:counter (atom 0)
                          :animals (atom {})}))
