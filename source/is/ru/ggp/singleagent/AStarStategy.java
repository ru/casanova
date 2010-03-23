package is.ru.ggp.singleagent;

import java.util.List;

import is.ru.ggp.singleagent.heuristic.HeuristicFactory;
import is.ru.ggp.singleagent.heuristic.IHeuristic;
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
    private boolean continueSearch = true;
    private IHeuristic heuristic;
    
    // flag used if we have found a new the best path.
    private boolean foundBestPath = false;

    // Constructor for the class
    public AStarStategy(){
        // Create instance of the open and closed list.
        this.closedList = new ClosedList(); 
        this.openList = new OpenList();
        this.heuristic = HeuristicFactory.getRelaxation();
    }
    
    @Override
    public void initMatch(Match initMatch){
        super.initMatch(initMatch);
        System.out.println("[A*] InitMatch executed.");

        // For the first node we set the cost to 0 and we calculate hauristic
        // value for the node.
        ValueNode node = new ValueNode(game.getTree().getRootNode());
        node.g = 0;
        node.h = this.heuristic.getHeuristic(node);

        this.openList.add(node);
        this.astar();
        
    }
    
    @Override
	public IMove getMove(IGameNode currentNode) 
    {
    	// Check if we want to continue the search from the init match phase.
    	// This will only be for the first time to combine the start time and the
    	// first play time.
    	ValueNode node = new ValueNode(currentNode);
    	if(continueSearch == false){
    		this.openList.add(node);
        }
    	else{
    		continueSearch = false;
    	}
    	// Do A star search.
    	this.astar();
    	
    	// Re-construct the path.
    	
    	// clear lists.
    	
    	// return the first action in the path found.
    	

    	
    	try {
            List<IMove[]> moves = match.getGame().getCombinedMoves(node.gameNode);
            return moves.get( random.nextInt( moves.size() ) )[playerNumber];
        }
        catch (InterruptedException e) {
            System.out.println("getMove() stopped by time.");
        }
        
        return null;
	}
    

    private void astar(){
    	int bestTerminalValue = -1;
    	
    	System.out.println("[A*] Astar search initalized.");
    	TimerFlag timer = match.getTimer();
    	 
    	while(!this.openList.isEmpty() && bestTerminalValue < 100 )
    	{
    		if(timer.interrupted())
    			return;
    		
    		ValueNode node = this.openList.getMostProminentGameNode();	
    		
    		// If we find a goal, then we stop the search
    		// and then we reconstruct the path.
    		if(node.gameNode.isTerminal()){
    			return;
    		}
    		// add the node to the close list.
    		this.closedList.addToList(node);
    		
    		try {
                List<IMove[]> moveList = game.getCombinedMoves(node.gameNode);

                // Loop through the node
                for (IMove[] move : moveList) {
                    ValueNode nextNode = new ValueNode(game.getNextNode(node.gameNode, move));
                    nextNode.parent = node;
                    nextNode.parentAction = move;


                    if(!this.closedList.contains(nextNode)){
                        // calculate the h value
                        int hValue =  nextNode.getHauristicValue();

                        // If the new node is not in the open list, then we add it.
                        boolean isBest;
                        if(!this.openList.contains(nextNode))
                            this.openList.add(nextNode);
                        else{    
                        }
                    }
                }
				
			} catch (InterruptedException e) {
				System.out.println("[A*] got InterruptedException while going through neighbours.");
                return;
			}
    	}
    }
}
