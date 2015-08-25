(ns designer.block-builder
  )


(defrecord Block [uuid name])
(defrecord Port [uuid type block desc rate])

