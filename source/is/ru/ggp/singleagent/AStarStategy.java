package is.ru.ggp.singleagent;

import is.ru.ggp.singleagent.search.AStar;
import is.ru.ggp.singleagent.search.ISearch;
import org.eclipse.palamedes.gdl.core.model.IGameNode;
import org.eclipse.palamedes.gdl.core.model.IMove;
import org.eclipse.palamedes.gdl.core.simulation.strategies.AbstractStrategy;
import org.eclipse.palamedes.gdl.core.simulation.Match;

public class AStarStategy extends AbstractStrategy {

    private ISearch astarSearch;

    // Constructor for the class

    /*
    todo: Rename this class to single agent strategy.
    this strategy will contain astar and monte carlo.
     */
    public AStarStategy() {

        // Create instance of a star.
        this.astarSearch = new AStar(game);
    }

    public void initMatch(Match initMatch) {
        super.initMatch(initMatch);
        
        // initalize the a star search.
        this.astarSearch.initSearch(initMatch);
    }

    public IMove getMove(IGameNode currentNode) {
       return this.astarSearch.getMove(currentNode);
    }
}