(ns is.ru.ggp.reasoning.reasoneradapter
  (:gen-class
   :name is.ru.ggp.reasoning.CasanovaReasoner
   :implements [org.eclipse.palamedes.gdl.core.model.IReasoner]
   :prefix reasoner-
  )
)


(defn reasoner-createFluent
  "Factory for IFluent objects from String parameter"
  [fluentStr]
  ( (println "IReasoner::createFluent Unimplemented!")
    (System/exit 0)))

(defn reasoner-createMove
  "Factory for IMove objects from String parameter"
  [moveStr]
  ( (println "IReasoner::createMove Unimplemented!")
    (System/exit 0)
  )
)

(defn reasoner-getGoalValue
  "Returns the goal Value of the given state. If no goal exists, returns -1"
  [role state]
  ( (println "IReasoner::getGoalValue Unimplemented!")
    (System/exit 0)
  )
)

(defn reasoner-getLegalMoves
  "Returns the legal Moves for the specified role in the given state."
  [role state]
  ( (println "IReasoner::getLegalMoves Unimplemented!")
    (System/exit 0)
  )
)

(defn reasoner-getNextState
  "Returns the next state given the current state and moves by all players"
  [state moves]
  ( (println "IReasoner::getNextState Unimplemented!")
    (System/exit 0)
  )
)