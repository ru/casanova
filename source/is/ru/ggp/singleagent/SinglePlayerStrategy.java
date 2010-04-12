package is.ru.ggp.singleagent;

import is.ru.ggp.singleagent.search.AStar;
import is.ru.ggp.singleagent.search.ISearch;

import org.eclipse.palamedes.gdl.core.model.IGameNode;
import org.eclipse.palamedes.gdl.core.model.IMove;
import org.eclipse.palamedes.gdl.core.model.IReasoner;
import org.eclipse.palamedes.gdl.core.simulation.TimerFlag;
import org.eclipse.palamedes.gdl.core.simulation.strategies.AbstractStrategy;
import org.eclipse.palamedes.gdl.core.simulation.Match;

public class SinglePlayerStrategy extends AbstractStrategy {

    private ISearch astarSearch;
    private ISearch mcuctSearch;
    private Thread thread;

    // Constructor for the class

    /*
    todo: Rename this class to single agent strategy.
    this strategy will contain astar and monte carlo.
     */
    public SinglePlayerStrategy() {

        // Create instance of a star.
        this.astarSearch = new AStar(game);
        this.thread = new Thread(this.astarSearch);
        //this.mcuctSearch = new MCUct(game);
    }

    public void initMatch(Match initMatch) {
        //super.initMatch(initMatch);
        this.match = initMatch;
        initMatch.getCurrentNode();

        TimerFlag timer = match.getTimer();
        this.astarSearch.setMatch(initMatch);
        this.thread.start();
        while(this.thread.isAlive() && this.astarSearch.isSolved() == false && timer.interrupted() == false)
        {

            //System.out.print(".");
        }

        this.astarSearch.stopSearch();

 


    }

    public IMove getMove(IGameNode currentNode) {
    	
        

        if(this.astarSearch.isSolved())
        {

            this.astarSearch.findNextMove();
            IMove m = this.astarSearch.getNextMove();
            System.out.println(m);
            return m;
        }
        else
        {
            this.astarSearch.setCurrentNode(currentNode);
            this.thread = new Thread(this.astarSearch);

            TimerFlag timer = match.getTimer();
            this.thread.start();
            while(this.thread.isAlive() && this.astarSearch.isSolved() == false && timer.interrupted() == false)
            {
                //System.out.println("");
            }

            this.astarSearch.stopSearch();


            IMove m = null;
            while(m == null)
                m = this.astarSearch.getNextMove();
            System.out.println(m);
            return m;

        }
    }
}
