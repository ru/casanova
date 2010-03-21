package is.ru.ggp.singleagent;

import java.util.List;

import org.eclipse.palamedes.gdl.core.model.IGameNode;
import org.eclipse.palamedes.gdl.core.model.IMove;
import org.eclipse.palamedes.gdl.core.simulation.strategies.AbstractStrategy;
import is.ru.ggp.singleagent.lists.IClosedList; 
import is.ru.ggp.singleagent.lists.IOpenList;

import is.ru.ggp.singleagent.lists.ClosedList; 
import is.ru.ggp.singleagent.lists.OpenList;




public class AStarStategy extends AbstractStrategy 
{
    // Member variables
    private IClosedList closedList;
    private IOpenList openList; 

    // Constructor for the class
    public AStarStategy()
    {
        // Create instance of the open and closed list.
        this.closedList = new ClosedList(); 
        this.openList = new OpenList();
    }
    
    @Override
	public IMove getMove(IGameNode currentNode) 
    {
        /*
        try {
            System.out.println("TEST");
            List<IMove[]> moves = match.getGame().getCombinedMoves(currentNode);
            return moves.get( random.nextInt( moves.size() ) )[playerNumber];
        }
        catch (InterruptedException e) {
            System.out.println("getMove() stopped by time.");
        }
        
        return null;
        */
        return null;
	}
	
	/**
	 * Implemenation of the A* algorithm.
	 * @param timebound
	 * @param currentNode
	 * @return
	 */
	private IMove aStar(int timebound, IGameNode currentNode){
		return null;
	}
}
