package is.ru.ggp.singleagent;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import is.ru.ggp.singleagent.heuristic.HeuristicFactory;
import is.ru.ggp.singleagent.heuristic.IHeuristic;
import org.eclipse.palamedes.gdl.core.model.IGameNode;
import org.eclipse.palamedes.gdl.core.model.IGameState;
import org.eclipse.palamedes.gdl.core.model.IMove;
import org.eclipse.palamedes.gdl.core.model.IReasoner;
import org.eclipse.palamedes.gdl.core.model.utils.GameNode;
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

    private boolean solved = false;
    private ValueNode bestValueNode = null;
    List<IMove> solvedMoves = new LinkedList();
    Stack<IMove> solvedMovesStack = new Stack<IMove>();
    // flag used if we have found a new the best path.


    // Constructor for the class
    public AStarStategy(){
        // Create instance of the open and closed list.
        this.closedList = new ClosedList(); 
        this.openList = new OpenList();

        // Calculate the heuristic value of the first node.
        this.heuristic = HeuristicFactory.getRelaxation();
    }
    
    @Override
    public void initMatch(Match initMatch){
        super.initMatch(initMatch);
        System.out.println("[A*] InitMatch executed.");

        // Create the initial node and calculate the heuristic value
        // and set the cost to 0 (where it is the first node).
        ValueNode node = new ValueNode(initMatch.getCurrentNode(), game); 
        node.g = 0;
        node.h = this.heuristic.getHeuristic(node);

        // Add the initial node to the open list and initialize the
        // start time A* search.
        this.openList.add(node);
        this.astar();
    }
    
    @Override
	public IMove getMove(IGameNode currentNode) 
    {

        // Check if we have already solved the game
        if(this.solved){
            IMove returnMove = this.solvedMovesStack.pop();
            return returnMove;
        }

        // Check if we want to continue the search from the init match phase.
    	// This will only be for the first time to combine the start time and the
    	// first play time.
    	ValueNode node = new ValueNode(currentNode, game);
    	if(continueSearch == false){
    		this.openList.add(node);
        }
    	else{
    		continueSearch = false;
    	}


        // Do A* search.

        this.astar();

        
    	
    	// todo: Re-construct the path.
    	// todo: clear lists.
    	// todo: return the first action in the path found.
        
        return this.mock(currentNode);
	}
    


    private IMove mock(IGameNode currentNode)
    {
            	/** XXX: All strategy relevant code goes in here. */
        try {
            List<IMove[]> moves = match.getGame().getCombinedMoves(currentNode);
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
    	 
        IReasoner reasoner = game.getReasoner();
        String player = game.getRoleNames()[0];



        while(!this.openList.isEmpty() && bestTerminalValue < 100 ){
    		if(timer.interrupted())
    			return;

            // Pick the best node from the fringe.
    		ValueNode node = this.openList.getMostProminentGameNode();
            

    		// If we find a goal, then we stop the search and then we reconstruct the path.
    		if(node.gameNode.getState().isTerminal()){
                if(this.bestValueNode == null){
                    System.out.println("[A*] Found the first goal value: " + node.getGoalValue());
                    this.bestValueNode = node;
                    if(this.bestValueNode.getGoalValue() == 100)
                    {
                        this.solved = true;
                        System.out.println("[A*] Game solved.");

                        ValueNode n = this.bestValueNode;
                        while(n != null){
                            if(n.parentAction == null)
                                break;
                            solvedMovesStack.add(n.parentAction);
                            n = n.parent;
                        }
                        return;
                    }
                }
                else{
                    int goalValue = node.gameNode.getState().getGoalValue(0);
                    // should this be GEQ?
                    if(node.getGoalValue() > this.bestValueNode.getGoalValue()){
                        this.bestValueNode = node;
                        System.out.println("[A*] Found node value with better value: " + node.getGoalValue());
                        if(this.bestValueNode.getGoalValue() == 100){
                            this.solved = true;
                            System.out.println("[A*] Game solved.");
                            ValueNode n = this.bestValueNode;
                            while(n != null){
                                if(n.parentAction == null)
                                    break;
                                solvedMovesStack.add(n.parentAction);
                                n = n.parent;
                            }

                            return;
                        }
                    }
                }
    		}


            // add the node to the close list.
    		this.closedList.add(node);
    		
    		try {
                // Loop through the possible action that the most promising node
                // on the fringe has.
                IMove[] moves = reasoner.getLegalMoves(player, node.gameNode.getState());
                for (IMove move : moves)
                {
                    // create new node for each action.
                    IMove[] m = new IMove[1];
                    m[0] = move;
                    IGameState newState = reasoner.getNextState(node.gameNode.getState(), m);
                    IGameNode  newGameNode = new GameNode();
                    newGameNode.setState(newState);
                    ValueNode nextNode = new ValueNode(newGameNode, game);

                    nextNode.parent = node;
                    nextNode.parentAction = move;

                    if(this.closedList.contains(nextNode)){
                        System.out.println("[A*] node already on closed list... go for next one!");   
                    }
                    else{

                        if(!this.openList.contains(nextNode)) // if the new node.
                        {
                            this.openList.add(nextNode);    
                        }
                        else // if the node is already on the open list..
                        {
                            ValueNode oldNewNode = this.openList.get(nextNode.getStateId());
                            if(oldNewNode.g + oldNewNode.h > nextNode.g + oldNewNode.h)
                            {
                                System.out.println("Node is better");
                            }
                        }
                        // put all the nodes on the open list.
                    }


                    /*
                    if(!this.closedList.contains(nextNode)){
                        // calculate the h value
                        double hValue =  nextNode.h;

                        // If the new node is not in the open list, then we add it.
                        boolean isBest;
                        if(!this.openList.contains(nextNode))
                            this.openList.add(nextNode);
                        else{    
                        }
                    }
                    */
                }
				
			} catch (InterruptedException e) {
				System.out.println("[A*] got InterruptedException while going through neighbours.");
                return;
			}
    	}
    }
}
