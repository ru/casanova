(ns is.ru.ggp.reasoning.engine
  (:require
    [clojure.zip :as z]
    [clojure.contrib.zip-filter :as zf]
    [clojure.contrib.seq-utils :as su])
  (:use
    [clojure.contrib.monads]))


(def game-model (ref {}))


(defn m-res
  "Wraps collection as a set or returns nil otherwise."
  [coll]
  (if (empty? coll)
    nil
    (set coll)))


(defn read-gdl
  "Reads a game description and wraps it in a quote, making it usable for evaluation."
  [filename]
  (str "'(" "\n" (slurp filename) "\n" ")"))


(defn eval-gdl
  "Evaluates game description into a list of lists."
  [filename]
  (eval (read-string (read-gdl filename))))


(defn variable? [sym]
  (.startsWith (name sym) "?"))


(defn var-to-keyword
  "Changes symbols which start with ? to keywords without ? character."
  [sym]
  (if (and (symbol? sym) (variable? sym))
    (with-meta (symbol (subs (name sym) 1)) {:var true})
    sym))


(defn cleanup-gdl
  "Transforms symbols which are variables to keywords (e.g. ?player -> :player) which makes for easier pre-processing."
  [gdl]
  (loop [loc (z/seq-zip gdl)]
    (if (z/end? loc)
      (z/root loc)
      (recur (z/next (z/edit loc var-to-keyword))))))


(defn partition-gdl
  "Partitions game description into roles, initial-state, user-defined views,
  next-states, legal-moves, goals and terminals."
  [filename]
  (let [gdl (cleanup-gdl (eval-gdl filename))
        filter-when-first (fn [first-item coll] (filter #(= first-item (first %)) coll))
        roles (map second (filter-when-first 'role gdl))
        initial-state (map second (filter-when-first 'init gdl))
        special-implications? #{'next 'legal 'goal 'terminal}
        implication-name (fn [i] (if (coll? (first i)) (ffirst i) (first i)))
        implications (map rest (filter-when-first '<= gdl))
        special-implications (filter #(special-implications? (implication-name %)) implications)
        user-defined-implications (filter #(not (special-implications? (implication-name %))) implications)
        next-states (filter #(= 'next (implication-name %)) special-implications)
        legal-moves (filter #(= 'legal (implication-name %)) special-implications)
        goals (map first (filter #(= 'goal (implication-name %)) special-implications))
        terminals (filter #(= 'terminal (implication-name %)) special-implications)]
    {:roles roles, :initial-state initial-state, :user-defined user-defined-implications,
     :next-states next-states, :legal-moves legal-moves, :goals goals, :terminals terminals}))


(defmacro expand-true-lookup
  [pred]
  (let [epred (eval pred)
        terms (map #(if (:var (meta %)) % `(quote ~%)) epred)
        variables (filter #(:var (meta %)) epred)
        result (if (not-empty variables) (zipmap (map keyword variables) variables) `(quote ~epred))]
    `(fn [~'state]
      (let [~'lookup (~'state (list ~@terms))]
        (if (not (nil? ~'lookup))
          ~result)))))


(defmacro expand-true-loop
  [pred bounded-vars]
  (let [epred (eval pred)
        ebounded-vars (eval bounded-vars)
        terms (map #(symbol (str "p" %)) (take (count epred) (iterate inc 0)))
        variables (filter #(:var (meta (second %))) (su/indexed epred))
        bounded-variables (filter #(ebounded-vars (second %)) variables)
        unbounded-variables (filter #(not (ebounded-vars (second %))) variables)
        constants (concat (filter #(not (:var (meta (second %)))) (su/indexed epred)) (map #(list (first %) (ebounded-vars (second %))) bounded-variables))
        constraints (map #(list '= (symbol (str "p" (first %))) `(quote ~(second %))) constants)
        expr (zipmap (map #(keyword (second %)) unbounded-variables) (map #(symbol (str "p" (first %))) unbounded-variables))]
    `(fn [~'state]
      (for [~'pred ~'state :let [[~@terms] ~'pred] :when (and ~@constraints)] ~expr))))


;(defn create-model
;  "Creates a clojure model of an game description."
;  [gdl]
;  )



(defmacro create-legal
  [legal]
  "Creates a function which evaluates legal moves based on the description of the legal predicate."
  (let [elegal (eval legal)
        head ((comp rest first) elegal)
        body (rest elegal)
        player (first head)
        action (second head)]
    ;(println "head" head#)
    ;(println "body" body#)
    ;(println "player" player# "meta" (meta player#))
    ;(println "action" action# "meta" (map meta action#))
    (if (:var (meta player))
      `(fn [~'state ~'player] true)
      `(fn [~'state ~'player]
        (if (= (quote ~player) ~'player)
          true)))))

;(defn legal
;  "Returns the legal moves a player in a given state."
;  [state player])

;;; alias for tic-tac-toe game description, useful on the repl
(def ttt "tictactoe.kif")