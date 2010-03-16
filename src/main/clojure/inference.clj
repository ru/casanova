(ns inference
  (:use [clojure.contrib.monads]))


(defn read-gdl
  "Reads a game description and wraps it in a quote, making it usable for evaluation."
  [filename]
  (str "'(" "\n" (slurp filename) "\n" ")"))


(defn eval-gdl
  "Evaluates game description into a list of lists."
  [filename]
  (eval (read-string (read-gdl filename))))


(defn partition-gdl
  "Partitions game description into roles, initial-state, user-defined views,
  next-states, legal-moves, goals and terminals."
  [filename]
  (let [gdl (eval-gdl filename)
        filter-when-first (fn [first-item coll] (filter #(= first-item (first %)) coll))
        roles (filter-when-first 'role gdl)
        initial-state (filter-when-first 'init gdl)
        special-implications? #{'next 'legal 'goal 'terminal}
        implications (filter-when-first '<= gdl)
        implication-name (fn [i] (let [r (rest i)] (if (coll? (first r)) (first (first r)) (first r))))
        special-implications (filter #(special-implications? (implication-name %)) implications)
        user-defined-implications (filter #(not (special-implications? (implication-name %))) implications)
        next-states (filter #(= 'next (implication-name %)) special-implications)
        legal-moves (filter #(= 'legal (implication-name %)) special-implications)
        goals (filter #(= 'goal (implication-name %)) special-implications)
        terminals (filter #(= 'terminal (implication-name %)) special-implications)]
    {:roles roles, :initial-state initial-state, :user-defined user-defined-implications,
     :next-states next-states, :legal-moves legal-moves, :goals goals, :terminals terminals}))


;;; alias for tic-tac-toe game description, useful on the repl
(def ttt "tictactoe.kif")