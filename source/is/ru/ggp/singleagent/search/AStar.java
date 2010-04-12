package is.ru.ggp.singleagent.search;

import is.ru.ggp.singleagent.common.ValueNode;
import is.ru.ggp.singleagent.heuristic.HeuristicFactory;
import is.ru.ggp.singleagent.heuristic.IHeuristic;
import is.ru.ggp.singleagent.lists.ClosedList;
import is.ru.ggp.singleagent.lists.IClosedList;
import is.ru.ggp.singleagent.lists.IOpenList;
import is.ru.ggp.singleagent.lists.OpenList;
import org.eclipse.palamedes.gdl.core.model.*;
import org.eclipse.palamedes.gdl.core.model.utils.GameNode;
import org.eclipse.palamedes.gdl.core.simulation.Match;
import org.eclipse.palamedes.gdl.core.simulation.TimerFlag;

import java.util.Stack;

public class AStar implements ISearch{
    // Member variables
    private IClosedList closedList;
    private IOpenList openList;
    private boolean continueSearch = true;
    private IHeuristic heuristic;
    private boolean solved = false;
    private boolean hasNotMovedYet = false;
    private ValueNode bestValueNode = null;
    private Stack<IMove> solvedMovesStack = new Stack<IMove>();
    private Match match;
    




    private IMove nextMove = null;
    private IGameNode cNode = null;
    private boolean stopSeach = false;
    private boolean initPhase = true;


    @Override
    public boolean isSolved() {
        return this.solved;
    }

    @Override
    public void run() {
        System.out.println("Thread starts");
        if(this.initPhase)
        {
            System.out.println("Thread start in init");
            this.initSearch();
            this.initPhase = false;
        }
        else{
            System.out.println("Thread start in search");
            this.findNextMove();
        }
    }

    @Override
    public void stopSearch() {
        this.stopSeach = true;
    }

    @Override
    public void setMatch(Match initMatch) {
        this.match = initMatch;
    }

    @Override
    public IMove getNextMove() {
        return nextMove;
    }

    @Override
    public void setCurrentNode(IGameNode currentNode) {
        this.cNode = currentNode;
    }

    public AStar(IGame game){

        // Create instance of the open and closed list.
        this.closedList = new ClosedList();
        this.openList = new OpenList();

        // Create instance of the heuristic.
        this.heuristic = HeuristicFactory.getRelaxation(game);
    }

    public void initSearch(){
        
        this.heuristic.readGoalStateFromMatch(match);

        // Create the initial node and calculate the heuristic value
        // and set the cost to 0 (where it is the first node).
        ValueNode node = new ValueNode(this.match.getCurrentNode(), this.match.getGame());
        node.g = 0;
        node.h = this.heuristic.getHeuristic(node);

        // Add the initial node to the open list and initialize the
        // start time A* search.
        this.openList.add(node);
        this.astar();
    }
    
    public int getGoalEstimate() {
        return 0;
    }

    public void findNextMove() {
        // Check if we have already solved the game
        if (this.solved && !this.hasNotMovedYet) {
            IMove returnMove = this.solvedMovesStack.pop();
            this.nextMove = returnMove;
            return;
        }
        hasNotMovedYet = true;

        // Check if we want to continue the search from the init match phase.
        // This will only be for the first time to combine the start time and the
        // first play time.
        ValueNode node = new ValueNode(this.cNode, this.match.getGame());
        node.h = this.heuristic.getHeuristic(node);
        node.g = 0;

        if (continueSearch == false) {
            this.openList.add(node);
        } else {
            continueSearch = false;
        }

        this.astar();


        //IMove returnMove = null;

        // We must select a move that we want to return.
        if (this.bestValueNode != null) {

            if (this.bestValueNode.getGoalValue() == 100) {
                System.out.println("[A*] using a solved path.");
                this.solvedMovesStack = this.reconstructPathFromNode(this.bestValueNode);
                this.hasNotMovedYet = false;
                this.nextMove = this.solvedMovesStack.pop();
                return;

            }
            else if (this.bestValueNode.getGoalValue() >= 30)
            {
            	//System.out.println("[A*] We pick move from best value"+bestValueNode.getGoalValue());
                this.nextMove = this.reconstructPathFromNode(this.bestValueNode).pop();
                if (this.nextMove == null) {
                    System.out.println("WTF!!");
                }
            }
            else
            	//System.out.println("[A*] We pick move from most prominent");
                this.nextMove = this.reconstructPathFromNode(this.openList.getMostProminentGameNode()).pop();
                if (this.nextMove == null) {
                    System.out.println(">> WARNING ACTION WAS NULL!");
                }

        } else {
            //System.out.println("[A*] We pick move from most prominent");
            this.nextMove = this.reconstructPathFromNode(this.openList.getMostProminentGameNode()).pop();
        }
        //System.out.println("Move returned:" + returnMove);
        this.closedList.clear();
        this.openList.clear();
        this.bestValueNode = null;
        //this.nextMove = returnMove;

        //if(this.r)
        System.out.println("next move set to: " + this.nextMove);
        return;
    }

    private void astar() {
        try{
        int bestTerminalValue = -1;

        String player = this.match.getGame().getRoleNames()[0];

        //game.getRoleNames()[0];

        System.out.println("[A*] Astar search initalized.");


        IReasoner reasoner = this.match.getGame().getReasoner();

        while (!this.openList.isEmpty() && bestTerminalValue < 100) {
            if (this.stopSeach){
                this.stopSeach = false;
                return;

            }
            
            // Pick the best node from the fringe.
            ValueNode node = this.openList.getMostProminentGameNode();

            // If we find a goal, then we stop the search and then we reconstruct the path.
            if (node.gameNode.getState().isTerminal()) {
                if (this.bestValueNode == null) {
                    //System.out.println("[A*] Found the first goal value: " + node.getGoalValue());
                    bestTerminalValue = node.getGoalValue();
                    this.bestValueNode = node;
                    if (this.bestValueNode.getGoalValue() == 100) {
                        this.solved = true;
                        System.out.println("[A*] Game solved.");
                        this.solvedMovesStack = this.reconstructPathFromNode(this.bestValueNode);
                        return;
                    }
                } else {
                    // should this be GEQ?
                    if (node.getGoalValue() > this.bestValueNode.getGoalValue()) {
                        this.bestValueNode = node;
                        bestTerminalValue = node.getGoalValue();
                       // System.out.println("[A*] Found node value with better value: " + node.getGoalValue());
                        if (this.bestValueNode.getGoalValue() == 100) {
                            this.solved = true;
                            System.out.println("[A*] Game solved.");
                            //ValueNode n = this.bestValueNode;
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
                for (IMove move : moves) {

                    // create new node for each action.
                    IMove[] m = new IMove[1];
                    m[0] = move;
                    IGameState moveState = reasoner.getNextState(node.gameNode.getState(), m);
                    IGameNode moveGameNode = new GameNode();
                    moveGameNode.setState(moveState);
                    ValueNode moveNode = new ValueNode(moveGameNode, this.match.getGame());

                    moveNode.h = this.heuristic.getHeuristic(moveNode);
                    moveNode.g = node.g + 1;
                    //double moveNode_heuristic = moveNode.h;
                    //System.out.println("Move node heuristic: " + moveNode.h);

                    moveNode.parent = node;
                    moveNode.parentAction = move;



                    if (this.closedList.contains(moveNode))
                    {
                        //System.out.println("[A*] node already on closed list... go for next one!");
                    } else {

                        // if the new node.
                        if (!this.openList.contains(moveNode)){
                            //System.out.println(">> adding new node to open list");
                            this.openList.add(moveNode);
                        }
                        else // if the node is already on the open list..
                        {
                            //System.out.println("node was already on open list");
                            ValueNode oldNewNode = this.openList.get(moveNode.getStateId());
                            if (oldNewNode.g +oldNewNode.h  < moveNode.g + moveNode.h){
                                oldNewNode.g = moveNode.g;
                                oldNewNode.h = moveNode.h;
                                oldNewNode.parent = moveNode.parent;
                                oldNewNode.parentAction = moveNode.parentAction;
                                this.openList.reload();
                            }

                        }
                    }
                }

            } catch (InterruptedException e) {
                System.out.println("[A*] got InterruptedException while going through neighbours.");
                return;
            }
        }
        }
        catch (Exception e)
        {
            System.out.println("WTF!!!");
        }
    }
    
    private Stack<IMove> reconstructPathFromNode(ValueNode node) {
        Stack<IMove> pathStack = new Stack<IMove>();
        ValueNode n = node;
        while (n != null) {
            if (n.parentAction == null)
                break;
            pathStack.add(n.parentAction);
            n = n.parent;
        }
        return pathStack;
    }
}
