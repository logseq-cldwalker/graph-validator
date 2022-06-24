(ns test
  (:require [clojure.test :as t :refer [deftest is]]
            [datascript.core :as d]
            [logseq.graph-parser.cli :as gp-cli]
            [logseq.graph-parser.text :as gp-text]
            [logseq.db.rules :as rules]
            [clojure.set :as set]
            [clojure.edn :as edn]
            ["path" :as path]))

(def db-conn (atom nil))
(def all-asts (atom nil))

(defn- setup-graph [dir]
  (println "Parsing graph" dir)
  (let [{:keys [conn asts]} (gp-cli/parse-graph dir {:verbose false})]
    (reset! db-conn conn)
    (reset! all-asts (mapcat :ast asts))
    (println "Ast node count:" (count @all-asts))))

(defn- extract-subnodes-by-pred [pred node]
  (cond
    (= "Heading" (ffirst node))
    (filter pred (-> node first second :title))

    ;; E.g. for subnodes buried in Paragraph
    (vector? (-> node first second))
    (filter pred (-> node first second))))

(defn- ast->block-refs [ast]
  (->> ast
       (mapcat (partial extract-subnodes-by-pred
                        #(and (= "Link" (first %))
                              (= "Block_ref" (-> % second :url first)))))
       (map #(-> % second :url second))))

(defn- ast->embed-refs [ast]
  (->> ast
       (mapcat (partial extract-subnodes-by-pred
                        #(and (= "Macro" (first %))
                              (= "embed" (:name (second %)))
                              (gp-text/get-block-ref (str (first (:arguments (second %))))))))
       (map #(-> % second :arguments first gp-text/get-block-ref))))

(deftest block-refs-are-valid
  (let [block-refs (ast->block-refs @all-asts)]
    (println "Found" (count block-refs) "block refs")
    (is (empty?
         (set/difference
          (set block-refs)
          (->> (d/q '[:find (pull ?b [:block/properties])
                      :in $ %
                      :where (has-property ?b :id)]
                    @@db-conn
                    (vals rules/query-dsl-rules))
               (map first)
               (map (comp :id :block/properties))
               set))))))

(deftest embed-block-refs-are-valid
  (let [embed-refs (ast->embed-refs @all-asts)]
    (println "Found" (count embed-refs) "embed block refs")
    (is (empty?
         (set/difference
          (set embed-refs)
          (->> (d/q '[:find (pull ?b [:block/properties])
                      :in $ %
                      :where (has-property ?b :id)]
                    @@db-conn
                    (vals rules/query-dsl-rules))
               (map first)
               (map (comp :id :block/properties))
               set))))))

(defn- ast->queries
  [ast]
  (->> ast
       (mapcat (fn [nodes]
                 (keep
                  (fn [subnode]
                    (when (= ["Custom" "query"] (take 2 subnode))
                      (get subnode 4)))
                  nodes)))))

(deftest advanced-queries-are-valid
  (let [query-strings (ast->queries @all-asts)]
    (println "Found" (count query-strings) "queries")
    (is (empty? (keep #(let [query (try (edn/read-string %)
                                     (catch :default _ nil))]
                         (when (nil? query) %))
                      query-strings))
        "Queries are valid EDN")

    (is (empty? (keep #(let [query (try (edn/read-string %)
                                     (catch :default _ nil))]
                         (when (not (contains? query :query)) %))
                      query-strings))
        "Queries have required :query key")))

;; run this function with: nbb-logseq -m test/run-tests
(defn run-tests [& args]
  (let [dir* (or (first args) ".")
        ;; Move up a directory since the script is run in subdirectory of a
        ;; project
        dir (if (path/isAbsolute dir*) dir* (path/join ".." dir*))]
    (setup-graph dir)
    (t/run-tests 'test)))
