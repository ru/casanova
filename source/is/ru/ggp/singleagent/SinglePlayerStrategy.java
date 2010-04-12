package is.ru.ggp.singleagent;

import is.ru.ggp.singleagent.search.AStar;
import is.ru.ggp.singleagent.search.ISearch;
import is.ru.ggp.singleagent.search.MCUct;

import org.eclipse.palamedes.gdl.core.model.IGameNode;
import org.eclipse.palamedes.gdl.core.model.IMove;
import org.eclipse.palamedes.gdl.core.simulation.strategies.AbstractStrategy;
import org.eclipse.palamedes.gdl.core.simulation.Match;

public class SinglePlayerStrategy extends AbstractStrategy {

    private ISearch astarSearch;
    private ISearch mcuctSearch;

    // Constructor for the class

    /*
    todo: Rename this class to single agent strategy.
    this strategy will contain astar and monte carlo.
     */
    public SinglePlayerStrategy() {

        // Create instance of a star.
        this.astarSearch = new AStar(game);
        //this.mcuctSearch = new MCUct(game);
    }

    public void initMatch(Match initMatch) {
        super.initMatch(initMatch);
        
        // initalize the a star search.
        this.astarSearch.initSearch(initMatch);
        //this.mcuctSearch.initSearch(initMatch);
    }

    public IMove getMove(IGameNode currentNode) {
    	return this.astarSearch.getMove(currentNode);
    	//return this.mcuctSearch.getMove(currentNode);
    }
}
