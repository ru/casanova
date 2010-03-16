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


(defn filter-when-first
  "Returns lists from a collection when the first item in the list matches a given paramter."
  [first-item coll]
  (filter #(= first-item (first %)) coll))


(def special-implications? #{'next 'legal 'goal 'terminal})


(defn build-model
  "Builds a model of a game description."
  [filename]
  (let [gdl (eval-gdl filename)
        roles (filter-when-first 'role gdl)
        initial-state (filter-when-first 'init gdl)
        implications (filter-when-first '<= gdl)
        implication-name (fn [i] (let [r (rest i)] (if (coll? (first r)) (first (first r)) (first r))))
        special-implications (filter #(special-implications? (implication-name %)) implications)
        user-defined-implications (filter #(not (special-implications? (implication-name %))) implications)
        next (filter #(= 'next (implication-name %)) special-implications)
        legal-moves (filter #(= 'legal (implication-name %)) special-implications)
        goals (filter #(= 'goal (implication-name %)) special-implications)
        terminals (filter #(= 'terminal (implication-name %)) special-implications)]
    (println roles)
    (println "********")
    (println initial-state)
    (println "********")
    (println implications)
    (println "********")
    (println special-implications)
    (println "********")
    (println user-defined-implications)
    (println "********")
    (println next)
    (println "********")
    (println legal-moves)
    (println "********")
    (println goals)
    (println "********")
    (println terminals)))


;;; alias for tic-tac-toe game description, useful on the repl
(def ttt "tictactoe.kif")