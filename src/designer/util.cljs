(ns designer.util)

(defn keyed
  "Turn a seq of maps into a map of maps, where the key of the new map equals the value at key"
  [k vs]
  (reduce (fn [a v] (assoc a (k v) v)) {} vs))


(defn by-name [coll name]
  "Get first item in sequence whose :name property equals `name`"
  (first (filter #(= name (get % :name)) coll)))


(defn get-keys
  "Return a vector of values in m corresponding to ks, in order"
  [m ks]
  (reduce (fn [s k] (conj s (m k))) [] ks))

(defn get-keys-in
  ([fluxmap path ks not-found]
   (map #(get-in % path not-found) (get-keys fluxmap ks)))
  ([fluxmap path ks]
   (get-keys-in fluxmap path ks nil)))


(defn trace
  ([x]
   (trace " vvvvv " x))
  ([msg x]
   (println (str "------ " msg " -------\n") x) x))
