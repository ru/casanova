package is.ru.ggp.singleagent.search;

import org.eclipse.palamedes.gdl.core.model.IGameNode;
import org.eclipse.palamedes.gdl.core.model.IMove;
import org.eclipse.palamedes.gdl.core.simulation.Match;

public interface ISearch extends Runnable{
    int getGoalEstimate();

    void findNextMove();

    IMove getNextMove();
    void setMatch(Match initMatch);
    void setCurrentNode(IGameNode currentNode);

    void stopSearch();
    boolean isSolved();


    void initSearch();

}
