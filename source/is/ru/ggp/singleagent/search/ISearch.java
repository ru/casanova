package is.ru.ggp.singleagent.search;

import org.eclipse.palamedes.gdl.core.model.IGameNode;
import org.eclipse.palamedes.gdl.core.model.IMove;
import org.eclipse.palamedes.gdl.core.simulation.Match;

public interface ISearch {
    int getGoalEstimate();
    IMove getMove(IGameNode currentNode);
    void initSearch(Match match);
    
}
