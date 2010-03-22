package is.ru.ggp.singleagent;

import java.util.List;

import org.eclipse.palamedes.gdl.core.model.IGameNode;
import org.eclipse.palamedes.gdl.core.model.IMove;
import org.eclipse.palamedes.gdl.core.simulation.strategies.AbstractStrategy;

import is.ru.ggp.singleagent.common.ValueNode;
import is.ru.ggp.singleagent.lists.IClosedList; 
import is.ru.ggp.singleagent.lists.IOpenList;
import org.eclipse.palamedes.gdl.core.simulation.TimerFlag;

import is.ru.ggp.singleagent.lists.ClosedList; 
import is.ru.ggp.singleagent.lists.OpenList;
import org.eclipse.palamedes.gdl.core.simulation.Match;



public class AStarStategy extends AbstractStrategy 
{
    // Member variables
    private IClosedList closedList;
    private IOpenList openList;
    private Match match; 

    // Constructor for the class
    public AStarStategy()
    {
        // Create instance of the open and closed list.
        this.closedList = new ClosedList(); 
        this.openList = new OpenList();
    }
    
    @Override
    public void initMatch(Match initMatch){
        super.initMatch(initMatch);
        System.out.println(">> kallad i init matach");
        this.match = initMatch;
        TimerFlag timer = match.getTimer();
        System.out.println(">> Thad var timer interrupt.");
    }
    
    @Override
	public IMove getMove(IGameNode currentNode) 
    {
        // Convert to value node which contains value
    	// and A* node helper functions.
    	ValueNode node = new ValueNode(currentNode);
    	
    	try {
            System.out.println(">> TEST");
            List<IMove[]> moves = match.getGame().getCombinedMoves(node.gameNode);
            return moves.get( random.nextInt( moves.size() ) )[playerNumber];
        }
        catch (InterruptedException e) {
            System.out.println("getMove() stopped by time.");
        }
        
        return null;
	}
    

    private void search(){
    }
}
