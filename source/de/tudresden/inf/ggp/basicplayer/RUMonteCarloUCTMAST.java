package de.tudresden.inf.ggp.basicplayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.palamedes.gdl.core.model.IGameNode;
import org.eclipse.palamedes.gdl.core.model.IGameState;
import org.eclipse.palamedes.gdl.core.model.IGameTree;
import org.eclipse.palamedes.gdl.core.model.IMove;
import org.eclipse.palamedes.gdl.core.simulation.Match;
import org.eclipse.palamedes.gdl.core.simulation.strategies.AbstractStrategy;
import org.eclipse.palamedes.gdl.core.simulation.strategies.FringeFrame;


public class RUMonteCarloUCTMAST extends AbstractStrategy {

    /**
     * Global parameters of the strategy
     */
    private static  double w_h    = 1.0;
    private static  double w_n    = 1.0;
    private static  double w_n1   = 1.0;
    private static  double MASTDistribution = 10;

    /**
     * Internal class that saves the evaluation values for a particular node.
     */
    private static  class Evaluation {
        int visitCounts = 0;
        double sumOfGoals  = 0;
        public  int[] buckets = new int[101];

        public  double getAvg() { return sumOfGoals / visitCounts; }
    }

    /**
     * Internal class that saves the information about unexpanded moves
     */
    private static  class UCTFringeFrame extends FringeFrame {

        public UCTFringeFrame(IGameNode gameNode) {
            super(gameNode);
        }
    }

    private static class MastMove {
        IGameNode node;
        int visitCounts = 0;
        double sumOfGoals = 0;
        double average;

        public void calcAvg() { average = sumOfGoals / visitCounts; }
    }

    /**
     * Strategy internal value storage.
     */
    Map<IGameNode, Evaluation>         eval;
    HashMap<IGameNode, UCTFringeFrame> fringe;
    // Stores global info on actions for all players
    ArrayList<HashMap<IMove, MastMove>>     actionsValues;
    int                                simCount;
    int                                moveCount;

    @Override
    public void initMatch(Match newMatch) {
        super.initMatch(newMatch);

        // create an internal simulator
        eval      = new HashMap<IGameNode, Evaluation>();
        fringe    = new HashMap<IGameNode, UCTFringeFrame>();
        actionsValues = new ArrayList<HashMap<IMove, MastMove>>();
        simCount  = 0;
        moveCount = 0;

        simulate();
    }

    private void simulate() {

        // simulation info - static (value localization)
        List<IGameNode> nodeHistory = new ArrayList<IGameNode>();
        List<IMove[]>   moveHistory = new ArrayList<IMove[]>();
        IGameTree       tree        = game.getTree();
        IGameNode       startNode   = match.getCurrentNode();

        try {

            game.getCombinedMoves(startNode);
            fringe.clear();
            fringe.put( startNode, new UCTFringeFrame(startNode) );
            eval.put  ( startNode, new Evaluation()              );

            int nextSimCountToPrint  = simCount+100;
            int lastPrintSimCount = simCount;
            long lastSimCountPrintTime = System.currentTimeMillis();


            while (true) {

                // reset simulation information
                nodeHistory.clear();
                moveHistory.clear();

                // set start node
                IGameNode simNode = startNode;
                nodeHistory.add(simNode);

                //**************************************************************
                // do a single simulation
                //**************************************************************
                while ( !simNode.isTerminal() ) {
                    simNode.setPreserve(true);

                    // test timer - do strategy disposal before leaving
                    if ( match.getTimer().interrupted() ) {
                        for (IGameNode node : nodeHistory)
                            node.setPreserve(false);
                        match.getCurrentNode().setPreserve(true);
                        return;
                    }

                    //**********************************************************
                    // choose the next move
                    //**********************************************************
                    List<IMove[]> moveList  = game.getCombinedMoves(simNode);
                    // Get moves for players
                    // 1st index = moves for player 1
                    IMove[] playerMoves = game.getLegalMoves(simNode)[playerNumber];
                    IMove[]       simMoves  = null;
                    boolean       nextFrame = false;

                    UCTFringeFrame frame = fringe.get(simNode);
                    if (frame != null ) {
                        // still moves left ?
                        // yes: Expansion Mode
                        if ( frame.hasUnexpandedMove() ) {
                            simMoves = frame.getRandomUnexpandedMove();

//                            System.out.println("[" + simNode.getDepth() + "] Expansion Mode " + simNode.hashCode() + " " + Arrays.toString(simMoves) + " --- "+ sb.toString());

                        // no: UCT Mode
                        } else {

                            nextFrame           = true;
                            IMove[]   bestMove  = moveList.get(0);
                            double    bestValue = 0;

                            for (IMove[] move : moveList) {
                                double uct = getUCTValue(
                                     eval.get(simNode),
                                     eval.get(tree.getNextNode(simNode, move)));
                                if (uct > bestValue) {
                                    bestMove  = move;
                                    bestValue = uct;
                                }
                            }
                            simMoves = bestMove;
//                            System.out.println("[" + simNode.getDepth() + "] UCT Mode ");
                        }

                    } else
                        // no fringe frame present -> Random Mode
                        simMoves = game.getRandomMove(simNode);

                    // TODO: Choose move from mastList
                    // TODO: ----------------
                    //IMove[] possibleMoves = moveList.get(playerNumber);
                    // Get possible moves for current player
                    IMove[] possibleMoves = game.getLegalMoves(simNode)[playerNumber];
                    double bestMastValue = -1;
                    int bestMoveIndex = -1;
                    
                    // Go through all moves for current player

                    for (int i = 0; i < playerMoves.length; i++)
                    {
                        IMove move = playerMoves[i];
                        // If found in Mast List and has better MAST value
                        MastMove mastMove = actionsValues.get(playerNumber).get(move);
                        if (mastMove == null)
                            continue;
                        IMove[] otherMoves = playerMoves;
                        otherMoves[i] = null;
                        double mastValue = getMastValue(mastMove, otherMoves);
                        
                        if (actionsValues.get(playerNumber).get(move) != null
                                && bestMastValue < mastValue)
                        {
                            // We replace the best move with current move
                            bestMastValue = mastValue;
                            bestMoveIndex = i;
                        }
                    }

                    // Some move
                    //moveList.get(0)[playerNumber] :
                    // Best move from playerNumber
                    //bestMove[playerNumber];

                    if (bestMoveIndex != -1)
                        simMoves = moveList.get(bestMoveIndex);
                    else
                        simMoves = game.getRandomMove(simNode);

                    // TODO: Add move to history
                    moveHistory.add(simMoves);

                    // generate next state and moves
                    simNode = game.getNextNode(simNode, simMoves);

                    // only save the evaluation for simNode if the parent node has a FringeFrame
                    // i.e. don't save anything if we are in the random move part of the simulation
                    if(frame != null) {
	                    // make sure we have a evaluation object for the node
	                    if (!eval.containsKey(simNode) )
	                        eval.put(simNode, new Evaluation());

                        // simulation bookkeeping
                        nodeHistory.add(simNode);

                        // parent finished and no fringe frame present -> new frame
                        if ( nextFrame && !simNode.isTerminal() && !fringe.containsKey(simNode)) {
                            game.getCombinedMoves(simNode);
                            fringe.put(simNode, new UCTFringeFrame(simNode));
//                            System.out.println("\t\tNew Fringe Frame " + simNode);
                        }
                    }
                }

                //**************************************************************
                // back propagate the goal value
                //**************************************************************
                // get my goal
                IGameState lastState = simNode.getState();
                int        myGoal    = lastState.getGoalValue(playerNumber);

                // back propagate the goal
                for (IGameNode node : nodeHistory) {

                    // update evaluation
                    Evaluation evaluation = eval.get(node);
                    if(evaluation==null){
                    	evaluation=new Evaluation();
                    	eval.put(simNode, evaluation);
                    }
                    evaluation.visitCounts++;
                    evaluation.sumOfGoals += myGoal;
                    evaluation.buckets[myGoal]++;

                    // now we can forget this node
                    node.setPreserve(false);
                }

                // TODO: increment move average for action a
                // X 1 1
                // No op
                // For each move in the tree                
                //for (IMove[] move : moveHistory)
                for (int x = 0; x < moveHistory.size(); x++)
                {
                    IMove[] move = moveHistory.get(x);
                    // For each move a player made
                    for (int i = 0; i < move.length; i++)
                    {

                        MastMove historyMove = actionsValues.get(i).get(move);
                        if (historyMove != null)
                        {
                            historyMove.visitCounts++;
                            historyMove.sumOfGoals += myGoal;
                            historyMove.calcAvg();
                        }
                        else
                        {
                            historyMove = new MastMove();
                            historyMove.visitCounts++;
                            historyMove.sumOfGoals += myGoal;
                            historyMove.node = nodeHistory.get(x);
                            actionsValues.get(i).put(move[i],historyMove);
                        }
                    }
                }

                simCount++;
                if(simCount == nextSimCountToPrint){
                	long currentTime=System.currentTimeMillis();
                	double avg_time=((double)(currentTime-lastSimCountPrintTime))/(simCount-lastPrintSimCount);
                	System.out.println("simCount: "+simCount+" avg_time: "+avg_time+"ms");
                	if(avg_time>0){
                		nextSimCountToPrint+=(int)(5000/avg_time)+1;
                	}else{
                		nextSimCountToPrint+=(simCount-lastPrintSimCount)*5000;
                	}
                	lastPrintSimCount=simCount;
                	lastSimCountPrintTime=currentTime;
                	printValues(startNode);
                }
            }
        }
        catch (InterruptedException e) {
            // do strategy disposal before leaving
            for (IGameNode node : nodeHistory)
                node.setPreserve(false);
            match.getCurrentNode().setPreserve(true);
        }
    }

    private void printValues(IGameNode currentNode){
        try {

	        // find best child node
	        List<IMove[]> moveList = game.getCombinedMoves(currentNode);
	        IMove[]       bestMove = null;
	        double        bestAvg  = -1;
	        StringBuilder sb       = new StringBuilder();

	        // iterate over all moves
	        for (IMove[] move : moveList) {
	            sb.append("\nmove" + Arrays.toString(move) );

	            IGameNode  nextNode = game.getNextNode(currentNode, move);
	            Evaluation nextEval = eval.get(nextNode);

	            // skip if node was not already evaluated or resolved
	            if (nextEval == null)
	                continue;

	            // did we found something better ?
	            double nextAvg  = nextEval.getAvg();
	            double variance = getVariance(nextEval.visitCounts, nextAvg, nextEval.buckets); // = 0;
	            if ( nextAvg > bestAvg) {
	                bestAvg      = nextAvg;
	                bestMove     = move;
	            }


	            sb.append("\tgoal["    + Math.round(nextAvg)   + "]");
	            sb.append("\tUCT["     + getUCTValue(eval.get(currentNode),
	                                                 nextEval) + "]");
	            sb.append("\tvisits["  + nextEval.visitCounts  + "]");
	            sb.append("\tvarianz[" + variance  + "]");
	        }

	        IMove result = (bestMove == null) ?
	                                           moveList.get(0)[playerNumber] :
	                                           bestMove[playerNumber];

	        System.out.println(sb.toString());
	        System.out.println( ">>> SMonteCarloUCT move[" + result    +
	                            "], bestAvg["              + bestAvg   +
	                            "], simulationCount["      + simCount  + "]");
        } catch (InterruptedException e) { }
    }

    @Override
    public IMove getMove(IGameNode currentNode) {
        simulate();

        try {

            // find best child node
            List<IMove[]> moveList = game.getCombinedMoves(currentNode);
            IMove[]       bestMove = null;
            double        bestAvg  = -1;
            StringBuilder sb       = new StringBuilder();

            // iterate over all moves
            for (IMove[] move : moveList) {
                sb.append("\nmove" + Arrays.toString(move) );

                IGameNode  nextNode = game.getNextNode(currentNode, move);
                Evaluation nextEval = eval.get(nextNode);

                // skip if node was not already evaluated or resolved
                if (nextEval == null)
                    continue;

                // did we found something better ?
                double nextAvg  = nextEval.getAvg();
                double variance = getVariance(nextEval.visitCounts, nextAvg, nextEval.buckets); // = 0;
//                UCTFringeFrame fringeFrame=fringe.get(nextNode);
//                if(fringeFrame!=null){
//                	variance=getVariance(fringeFrame.buckets);
//                }else{
//                	System.out.println("no fringe frame: "+nextNode);
//                }
                if ( nextAvg > bestAvg) {
                    bestAvg      = nextAvg;
                    bestMove     = move;
                    reliability  = variance;
                    expectedGoal = bestAvg;
                }


                sb.append("\tgoal["    + Math.round(nextAvg)   + "]");
                sb.append("\tUCT["     + getUCTValue(eval.get(currentNode),
                                                     nextEval) + "]");
                sb.append("\tvisits["  + nextEval.visitCounts  + "]");
                sb.append("\tvarianz[" + variance  + "]");
            }

            moveCount++;
            IMove result = (bestMove == null) ?
                                               moveList.get(0)[playerNumber] :
                                               bestMove[playerNumber];

            System.out.println(sb.toString());
            System.out.println( ">>> SMonteCarloUCT move[" + result    +
                                "], step["                 + moveCount +
                                "], bestAvg["              + bestAvg   +
                                "], simulationCount["      + simCount  + "]");
            return result;
        }
        catch (InterruptedException e) {
        }
        return null;
    }

	/**
     * Returns the UCT Value:
     *
     *  uct = (w_h * h) + sqrt( ln(w_n * n) / (w_n1 * n1)  )
     *
     *  h  - heuristic value (normalized to a range of [0 ... 1])
     *  n  - number of visits for the parent node
     *  n1 - number of visits for the child  node
     *  w_ - weights for the different values
     */
    private static  double getUCTValue( Evaluation evalParent,
                                             Evaluation evalChild ) {
         double h  = (evalChild.getAvg() / 100) * w_h;
         double n  = evalParent.visitCounts     * w_n;
         double n1 = evalChild.visitCounts      * w_n1;
        return h + Math.sqrt( Math.log(n) / n1 );
    }

    /**
     * Returns the variance of the goals.
     */
    private static  double getVariance(int visits, double avg, int[] buckets) {

        double variance = 0;
        for (int i = 0; i < 101; i++)
            variance += ((i - avg) * (i - avg)) * buckets[i];

        return Math.sqrt(variance / visits);
    }

    private double getMastValue(MastMove move, IMove[] possibleActions)
    {
        double upperValue = Math.pow(Math.E, (move.average / MASTDistribution));
        // All possible actions in given state
        double lowerValue = 1;
        
        for (int i = 0; i < possibleActions.length; i++)
        {
            if (possibleActions[i] != null && actionsValues.get(playerNumber).get(possibleActions) != null)
                lowerValue += actionsValues.get(playerNumber).get(possibleActions).average;
        }

        return upperValue / lowerValue;
    }

}


