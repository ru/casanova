package de.tudresden.inf.ggp.basicplayer;

import java.util.List;

import org.eclipse.palamedes.gdl.core.model.IGameNode;
import org.eclipse.palamedes.gdl.core.model.IMove;
import org.eclipse.palamedes.gdl.core.model.utils.GameNodeTree;
import org.eclipse.palamedes.gdl.core.simulation.strategies.AbstractStrategy;


public class MyRandomStrategy extends AbstractStrategy {

    public GameNodeTree tree = new GameNodeTree();

    @Override
    public IMove getMove(IGameNode currentNode) {
    	/** XXX: All strategy relevant code goes in here. */
    // TODO: MAKE RECURSIVE
        try {
            List<IMove[]> moves = match.getGame().getCombinedMoves(currentNode);
            return moves.get( random.nextInt( moves.size() ) )[playerNumber];
        }
        catch (InterruptedException e) {
            System.out.println("findNextMove() stopped by time.");
        }
        
        return null;
    }
    
}
