(ns inference
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
    (keyword (subs (name sym) 1))
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
        implications (map rest (filter-when-first '<= gdl))
        implication-name (fn [i] (if (coll? (first i)) (ffirst i) (first i)))
        special-implications (filter #(special-implications? (implication-name %)) implications)
        user-defined-implications (filter #(not (special-implications? (implication-name %))) implications)
        next-states (filter #(= 'next (implication-name %)) special-implications)
        legal-moves (map first (filter #(= 'legal (implication-name %)) special-implications))
        goals (map first (filter #(= 'goal (implication-name %)) special-implications))
        terminals (filter #(= 'terminal (implication-name %)) special-implications)]
    {:roles roles, :initial-state initial-state, :user-defined user-defined-implications,
     :next-states next-states, :legal-moves legal-moves, :goals goals, :terminals terminals}))

;(def smu
;  (fn [state__5857__auto__]
;    (clojure.core/for [pred__5858__auto__ state__5857__auto__ :let [[p0 p1 p2 p3] pred__5858__auto__] :when (and (= p0 'cell) (= p3 'b))] {:y 2, 2 1})))

(defmacro expand-true
  [pred vars]
  (let [param (fn [i] (symbol (format "p%s" i)))
        ps (vec (map param (take (count pred) (iterate inc 0))))
        variables (filter #(keyword? (second %)) (su/indexed pred))
        constants (filter #(not (keyword? (second %))) (su/indexed pred))
        expr (zipmap (map second variables) (map first variables))]
    (list `(fn [state#]
      (for [pred# state# :let [~ps pred#] :when ~(cons 'and (map #(list '= (symbol (param (first %))) (symbol (str "'" (name (second %))))) constants))] ~expr)))))
; :when ~(cons 'and (map #(list '= (symbol (str "'" (name (second %))))) constants))

; for [pred state :let [[p1 p2 p3 p4] pred] :when (and (= p1 'cell) (= p4 'b))] {:x p2 :y p3})
;(defmacro create-legal-move
;  "Create clojure function for a legal move based on a legal move in the game description."
;  [legal]
;  `(let [zipper# (z/seq-zip ~legal)
;         [_# player# action#] (z/left zipper#)
;         rules# (z/left zipper#)]
;    (println (z/node zipper#))
;    (fn [state player]
;      (domonad maybe-m
;        []))))



;  `(let [lz# (z/seq-zip ~legal)
;         [_ player# action#] (z/left lz#)
;         playvar# (variable? player#)
;         rules# (z/rights (z/left lz#))
;         all-rules# (zf/descendants legal#)]
;    (println ~legal)
;    lz#))


;(defn create-model
;  "Creates a clojure model of an game description."
;  [gdl]
;  )

;(defn legal
;  "Returns the legal moves a player in a given state."
;  [state player])

;;; alias for tic-tac-toe game description, useful on the repl
(def ttt "tictactoe.kif")