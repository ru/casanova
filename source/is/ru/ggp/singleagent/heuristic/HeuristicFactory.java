package is.ru.ggp.singleagent.heuristic;

import org.eclipse.palamedes.gdl.core.model.IGame;

public class HeuristicFactory {
    public static IHeuristic getRelaxation(IGame game){
        return new RelaxationHeuristic(game);
    }
}
