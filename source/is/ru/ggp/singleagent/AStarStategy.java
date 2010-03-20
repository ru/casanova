package is.ru.ggp.singleagent;

import java.util.List;

import org.eclipse.palamedes.gdl.core.model.IGameNode;
import org.eclipse.palamedes.gdl.core.model.IMove;
import org.eclipse.palamedes.gdl.core.simulation.strategies.AbstractStrategy;

public class AStarStategy extends AbstractStrategy 
{

	@Override
	public IMove getMove(IGameNode currentNode) {
    	/** XXX: All strategy relevant code goes in here. */
        try {
            System.out.println("TEST");
            List<IMove[]> moves = match.getGame().getCombinedMoves(currentNode);
            return moves.get( random.nextInt( moves.size() ) )[playerNumber];
        }
        catch (InterruptedException e) {
            System.out.println("getMove() stopped by time.");
        }
        
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
