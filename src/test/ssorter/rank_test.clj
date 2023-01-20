(ns ssorter.rank
  (:require [clojure.test :refer [deftest is]]
            [jsonista.core :as j]))


(def mapper
  (j/object-mapper
   {:encode-key-fn name
    :decode-key-fn keyword}))


(declare decode-v)
(defn decode-kv [[left right]]
  (let [decoded-left (decode-v left)
        decoded-right (decode-v right)]
    (if (nil? decoded-left)
      nil
      {(if (string? decoded-left)
         (keyword decoded-left)
         decoded-left)
       decoded-right})))

(defn decode-v [[left right]]
  (case left
    "table" (let [mapped (into {} (map decode-kv right))]
              (if (every? #(number? (key %)) mapped)
                (vec (map second mapped))
                mapped))
    "number" (Integer/parseInt right)
    "string" right
    "boolean" (Boolean/valueOf right)
    "metatable" nil
    "recursion" nil
    "function" nil))

(defn parse-file [fname]
  (-> (slurp fname)
      (j/read-value mapper)
      :lines
      ffirst
      decode-v))

(def items (parse-file "src/test/ssorter/items.json"))
(def votes (parse-file "src/test/ssorter/votes.json"))

(def fruits-id "16c5f113-66cc-4514-8df0-4092a99285c0")
(def mtg-id "fdd74412-92e4-460f-ae80-19d6befef509")

(defn votes-for [tag_id]
  (->> votes
       (filter (comp #{tag_id} :tag_id))))

(defn items-in [votes]
  (distinct (flatten (map (juxt :item_a :item_b) votes))))

(def winners (let [votes (votes-for fruits-id)]
               (sorted (items-in votes) votes)))

(defn print-names [li]
  (for [[k _] li]
    (->> items
         (filter (comp #{k} :id))
         (map :name)
         first)))

(print-names winners)

;; with symmetric
(print-names '(["f70a67cd-249e-45d2-af9b-b88e3e0e0689" 0.0495425]
               ["4d2744b5-4f0a-445d-8570-1b60e79d6f21" 0.0495425]
               ["af723531-71b7-44c8-ab0f-db78560e4242" 0.0495425]
               ["e7fadbe6-ec5b-4f75-bf69-2c1f3e16b8ee" 0.0495425]
               ["131434d6-ee10-4bb4-9b1e-32e5da1285f2" 0.047417]
               ["d86fe563-b277-4a69-8090-e97bef3fe04f" 0.047304]
               ["4f39f686-ebee-49ac-bef3-cb2e1a877102" 0.0471778]
               ["cb2331cf-d870-442e-ace3-5aa812642e37" 0.0471778]
               ["090c7278-c0a3-4450-a32d-c01cfedb9e88" 0.0453835]
               ["75860827-fe0c-4754-8298-accb2a3c43c7" 0.0452515]
               ["72ccf932-3266-4a07-aaac-504e6df82ff3" 0.0452339]
               ["ea44a300-c99a-49f7-90a4-299d7cdd01e7" 0.0450426]
               ["48ab47c1-704f-49be-a0ad-8e6c767f10ca" 0.0450426]
               ["e763226b-9e4c-4d99-b3cb-18f4641d8383" 0.0429801]
               ["ca0d6462-d9f8-49ac-9d39-d68fb5cfb3ce" 0.0429618]
               ["4b94fa17-0944-4b2c-9855-08982c810dc6" 0.0429618]
               ["c2a4cbfa-4409-424d-bcc0-8a4a9109627c" 0.0428355]
               ["a0514ff9-6111-4e10-9ee6-86d0704fc13f" 0.0409466]
               ["307e2d99-dbf4-4398-bd01-d3c8c306a00a" 0.0409466]
               ["c1623a81-c235-40f3-8bcc-39256a52e1c0" 0.0408456]
               ["c30ee1e7-4c2c-4efd-8911-af7b0b628f74" 0.0408456]
               ["474c7909-8ae1-4844-8e79-2a7d79662f98" 0.0406057]))

;; with filtering (faster)
(print-names '(["cb2331cf-d870-442e-ace3-5aa812642e37" 0.0592779]
                ["75860827-fe0c-4754-8298-accb2a3c43c7" 0.0592473]
                ["d86fe563-b277-4a69-8090-e97bef3fe04f" 0.054254]
                ["f70a67cd-249e-45d2-af9b-b88e3e0e0689" 0.0538552]
                ["c2a4cbfa-4409-424d-bcc0-8a4a9109627c" 0.0509574]
                ["307e2d99-dbf4-4398-bd01-d3c8c306a00a" 0.050888]
                ["e763226b-9e4c-4d99-b3cb-18f4641d8383" 0.0493789]
                ["4d2744b5-4f0a-445d-8570-1b60e79d6f21" 0.0492548]
                ["a0514ff9-6111-4e10-9ee6-86d0704fc13f" 0.0481887]
                ["4b94fa17-0944-4b2c-9855-08982c810dc6" 0.0475483]
                ["72ccf932-3266-4a07-aaac-504e6df82ff3" 0.047384]
                ["4f39f686-ebee-49ac-bef3-cb2e1a877102" 0.046155]
                ["af723531-71b7-44c8-ab0f-db78560e4242" 0.0447409]
                ["c30ee1e7-4c2c-4efd-8911-af7b0b628f74" 0.0441236]
                ["ca0d6462-d9f8-49ac-9d39-d68fb5cfb3ce" 0.0412898]
                ["c1623a81-c235-40f3-8bcc-39256a52e1c0" 0.0412403]
                ["474c7909-8ae1-4844-8e79-2a7d79662f98" 0.0409446]
                ["e7fadbe6-ec5b-4f75-bf69-2c1f3e16b8ee" 0.0387164]
                ["48ab47c1-704f-49be-a0ad-8e6c767f10ca" 0.0384254]
                ["090c7278-c0a3-4450-a32d-c01cfedb9e88" 0.0372517]
                ["ea44a300-c99a-49f7-90a4-299d7cdd01e7" 0.0370476]
                ["131434d6-ee10-4bb4-9b1e-32e5da1285f2" 0.0181178]))




;; with commented out
(["75860827-fe0c-4754-8298-accb2a3c43c7" 0.0588331]
 ["cb2331cf-d870-442e-ace3-5aa812642e37" 0.0585533]
 ["d86fe563-b277-4a69-8090-e97bef3fe04f" 0.0537726]
 ["f70a67cd-249e-45d2-af9b-b88e3e0e0689" 0.0534747]
 ["307e2d99-dbf4-4398-bd01-d3c8c306a00a" 0.0505415]
 ["c2a4cbfa-4409-424d-bcc0-8a4a9109627c" 0.0503585]
 ["e763226b-9e4c-4d99-b3cb-18f4641d8383" 0.049079]
 ["4d2744b5-4f0a-445d-8570-1b60e79d6f21" 0.0490011]
 ["a0514ff9-6111-4e10-9ee6-86d0704fc13f" 0.0476369]
 ["4b94fa17-0944-4b2c-9855-08982c810dc6" 0.0474006]
 ["72ccf932-3266-4a07-aaac-504e6df82ff3" 0.0473705]
 ["4f39f686-ebee-49ac-bef3-cb2e1a877102" 0.0459611]
 ["af723531-71b7-44c8-ab0f-db78560e4242" 0.044492]
 ["c30ee1e7-4c2c-4efd-8911-af7b0b628f74" 0.0436627]
 ["c1623a81-c235-40f3-8bcc-39256a52e1c0" 0.0410783]
 ["ca0d6462-d9f8-49ac-9d39-d68fb5cfb3ce" 0.0408556]
 ["474c7909-8ae1-4844-8e79-2a7d79662f98" 0.0408254]
 ["e7fadbe6-ec5b-4f75-bf69-2c1f3e16b8ee" 0.038575]
 ["48ab47c1-704f-49be-a0ad-8e6c767f10ca" 0.0382322]
 ["090c7278-c0a3-4450-a32d-c01cfedb9e88" 0.0371496]
 ["ea44a300-c99a-49f7-90a4-299d7cdd01e7" 0.0364379]
 ["131434d6-ee10-4bb4-9b1e-32e5da1285f2" 0.018403])

(deftest pagerank []
  (is (= 3 3)))
