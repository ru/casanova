package is.ru.ggp.singleagent;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import is.ru.ggp.singleagent.heuristic.HeuristicFactory;
import is.ru.ggp.singleagent.heuristic.IHeuristic;
import org.eclipse.palamedes.gdl.core.ast.RuleGoal;
import org.eclipse.palamedes.gdl.core.model.IGameNode;
import org.eclipse.palamedes.gdl.core.model.IGameState;
import org.eclipse.palamedes.gdl.core.model.IMove;
import org.eclipse.palamedes.gdl.core.model.IReasoner;
import org.eclipse.palamedes.gdl.core.model.utils.Game;
import org.eclipse.palamedes.gdl.core.model.utils.GameNode;
import org.eclipse.palamedes.gdl.core.simulation.strategies.AbstractStrategy;

import is.ru.ggp.singleagent.common.ValueNode;
import is.ru.ggp.singleagent.lists.IClosedList; 
import is.ru.ggp.singleagent.lists.IOpenList;
import org.eclipse.palamedes.gdl.core.simulation.TimerFlag;

import is.ru.ggp.singleagent.lists.ClosedList; 
import is.ru.ggp.singleagent.lists.OpenList;
import org.eclipse.palamedes.gdl.core.simulation.Match;
import org.eclipse.palamedes.kif.core.ast.KIFSeq;

public class AStarStategy extends AbstractStrategy 
{
    // Member variables
    private IClosedList closedList;
    private IOpenList openList;
    private boolean continueSearch = true;
    private IHeuristic heuristic;

    private boolean solved = false;
    private boolean hasNotMovedYet = false;
    private ValueNode bestValueNode = null;
    private Stack<IMove> solvedMovesStack = new Stack<IMove>();
    //private List<String> goalStatePredicates;

    // Constructor for the class
    public AStarStategy(){
        // Create instance of the open and closed list.
        this.closedList = new ClosedList(); 
        this.openList = new OpenList();
        // Calculate the heuristic value of the first node.
        this.heuristic = HeuristicFactory.getRelaxation(game);
    }

    public void initMatch(Match initMatch){
        super.initMatch(initMatch);
        System.out.println("[A*] InitMatch executed.");
        System.out.println("MyPlayer created the game.");

        // Find the goal predicates from the GDL.
        this.heuristic.readGoalStateFromMatch(match);
        
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
    
	public IMove getMove(IGameNode currentNode)
    {
        // Check if we have already solved the game
        if(this.solved && !this.hasNotMovedYet){
            IMove returnMove = this.solvedMovesStack.pop();
            return returnMove;
        }
        hasNotMovedYet = true;

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

        this.astar();
        
        IMove returnMove = null;

        if(this.bestValueNode !=null){                   
            System.out.println("[A*] Using best value node with value: " + this.bestValueNode.getGoalValue());
            if(this.bestValueNode.getGoalValue() == 100){
                this.solvedMovesStack = this.reconstructPathFromNode(this.bestValueNode);
                this.hasNotMovedYet = false;
                return this.solvedMovesStack.pop();
                
            }
            returnMove = this.reconstructPathFromNode(this.bestValueNode).pop();
        }
        else{
            System.out.println("[A*] We pick move from most prominent");
        	returnMove = this.reconstructPathFromNode(this.openList.getMostProminentGameNode()).pop();
        }
        System.out.println("Move returned:"+returnMove);
        this.closedList.clear();
        this.openList.clear();
        this.bestValueNode = null;
        return returnMove;
	}
    
    private Stack<IMove> reconstructPathFromNode(ValueNode node){
        Stack<IMove> pathStack = new Stack<IMove>(); 
        ValueNode n = node;
        while(n != null){
        	//System.out.println("Parent Action:"+n.parentAction);
            if(n.parentAction == null)
                break;
           pathStack.add(n.parentAction);
            n = n.parent;
        }
        return pathStack;
    }

    


























    private void astar(){
        int bestTerminalValue = -1;
    	String player = game.getRoleNames()[0];
    	System.out.println("[A*] Astar search initalized.");
    	TimerFlag timer = match.getTimer();
        IReasoner reasoner = game.getReasoner();
        
        while(!this.openList.isEmpty() && bestTerminalValue < 100 ){
    		if(timer.interrupted())
    			return;

            // Pick the best node from the fringe.
    		ValueNode node = this.openList.getMostProminentGameNode();

    		// If we find a goal, then we stop the search and then we reconstruct the path.
    		if(node.gameNode.getState().isTerminal()){
                if(this.bestValueNode == null){
                    System.out.println("[A*] Found the first goal value: " + node.getGoalValue());
                    bestTerminalValue = node.getGoalValue();
                    this.bestValueNode = node;
                    if(this.bestValueNode.getGoalValue() == 100)
                    {
                        this.solved = true;
                        System.out.println("[A*] Game solved.");
                        this.solvedMovesStack = this.reconstructPathFromNode(this.bestValueNode);
                        return;
                    }
                }
                else{
                    // should this be GEQ?
                    if(node.getGoalValue() > this.bestValueNode.getGoalValue()){
                        this.bestValueNode = node;
                        bestTerminalValue = node.getGoalValue();
                        System.out.println("[A*] Found node value with better value: " + node.getGoalValue());
                        if(this.bestValueNode.getGoalValue() == 100){
                            this.solved = true;
                            System.out.println("[A*] Game solved.");
                            ValueNode n = this.bestValueNode;
                            this.solvedMovesStack = this.reconstructPathFromNode(this.bestValueNode);
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
                }
				
			} catch (InterruptedException e) {
				System.out.println("[A*] got InterruptedException while going through neighbours.");
                return;
			}
    	}
    }
}