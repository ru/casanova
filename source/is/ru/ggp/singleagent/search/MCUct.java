package is.ru.ggp.singleagent.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.palamedes.gdl.core.model.*;
import org.eclipse.palamedes.gdl.core.simulation.Match;
import org.eclipse.palamedes.gdl.core.simulation.strategies.FringeFrame;

public abstract class MCUct implements ISearch {
	
    private Match match;
    
    /**
     * Global parameters of the strategy
     */
    private static final double w_h    = 1.0;
    private static final double w_n    = 1.0;
    private static final double w_n1   = 1.0;

	private double reliability;
    private double heuristic;
    private double expectedGoal;
    private int    maxSteps;
    private int    playerNumber;
    
    public MCUct(IGame game) {
        this.setHeuristic(-1);
        this.setReliability(-1);
        this.setExpectedGoal(0);
    }
    
    public int getGoalEstimate() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Internal class that saves the evaluation values for a particular node.
     */
    private static final class Evaluation {
        int visitCounts = 0;
        double sumOfGoals  = 0;
        public final int[] buckets = new int[101];
        
        public final double getAvg() { return sumOfGoals / visitCounts; }
    }

    /**
     * Internal class that saves the information about unexpanded moves
     */
    private static final class UCTFringeFrame extends FringeFrame {
        
        public UCTFringeFrame(IGameNode gameNode) {
            super(gameNode);
        }        
    }
    
    /**
     * Strategy internal value storage.
     */
    Map<IGameNode, Evaluation>         eval;
    HashMap<IGameNode, UCTFringeFrame> fringe;
    int                                simCount;
    int                                moveCount;


    public void initSearch(Match match) {

    	this.match = match;
    	
        // create an internal simulator
        eval      = new HashMap<IGameNode, Evaluation>();
        fringe    = new HashMap<IGameNode, UCTFringeFrame>();
        simCount  = 0;
        moveCount = 0;

        simulate();
    }
    
  
    public IMove findNextMove(IGameNode currentNode) {
        simulate();

        try {

            // find best child node
            List<IMove[]> moveList = this.match.getGame().getCombinedMoves(currentNode);
            IMove[]       bestMove = null;
            double        bestAvg  = -1;
            StringBuilder sb       = new StringBuilder();

            // iterate over all moves
            for (IMove[] move : moveList) {
                sb.append("\nmove" + Arrays.toString(move) );

                IGameNode  nextNode = this.match.getGame().getNextNode(currentNode, move);
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
                    setReliability(variance);
                    setExpectedGoal(bestAvg);
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
            System.out.println( ">>> SP MCUct move[" + result    +
                                "], step["                 + moveCount +
                                "], bestAvg["              + bestAvg   +
                                "], simulationCount["      + simCount  + "]");
            return result;
        }
        catch (InterruptedException e) {
        }
        return null;
    }
    
    //Simulation
    private void simulate() {

        // simulation info - static (value localization)
        List<IGameNode> nodeHistory = new ArrayList<IGameNode>();
        IGameTree       tree        = this.match.getGame().getTree();
        IGameNode       startNode   = match.getCurrentNode();        

        try {

        	this.match.getGame().getCombinedMoves(startNode); 
            fringe.clear();
            fringe.put( startNode, new UCTFringeFrame(startNode) );
            eval.put  ( startNode, new Evaluation()              );

            int nextSimCountToPrint  = simCount+100;
            int lastPrintSimCount = simCount;
            long lastSimCountPrintTime = System.currentTimeMillis();

            
            while (true) {

                // reset simulation information
                nodeHistory.clear();

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
                    List<IMove[]> moveList  = this.match.getGame().getCombinedMoves(simNode);
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
                        simMoves = this.match.getGame().getRandomMove(simNode);
                    

                    // generate next state and moves
                    simNode = this.match.getGame().getNextNode(simNode, simMoves);

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
                        	this.match.getGame().getCombinedMoves(simNode);
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
	        List<IMove[]> moveList = this.match.getGame().getCombinedMoves(currentNode);
	        IMove[]       bestMove = null;
	        double        bestAvg  = -1;
	        StringBuilder sb       = new StringBuilder();
	
	        // iterate over all moves
	        for (IMove[] move : moveList) {
	            sb.append("\nmove" + Arrays.toString(move) );
	
	            IGameNode  nextNode = this.match.getGame().getNextNode(currentNode, move);
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
	        System.out.println( ">>> SP MCUct move[" + result    +
	                            "], bestAvg["              + bestAvg   +
	                            "], simulationCount["      + simCount  + "]");
        } catch (InterruptedException e) { }
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
    private static final double getUCTValue( Evaluation evalParent,
                                             Evaluation evalChild ) {
        final double h  = (evalChild.getAvg() / 100) * w_h;
        final double n  = evalParent.visitCounts     * w_n;
        final double n1 = evalChild.visitCounts      * w_n1;
        return h + Math.sqrt( Math.log(n) / n1 );
    }

    /**
     * Returns the variance of the goals. 
     */
    private static final double getVariance(int visits, double avg, int[] buckets) {

        double variance = 0;        
        for (int i = 0; i < 101; i++) 
            variance += ((i - avg) * (i - avg)) * buckets[i];
        
        return Math.sqrt(variance / visits);
    }

    
    /*
     * Getter Setter Just In Case we want to use.............
     */
	public void setReliability(double reliability) {
		this.reliability = reliability;
	}

	public double getReliability() {
		return reliability;
	}

	public void setExpectedGoal(double expectedGoal) {
		this.expectedGoal = expectedGoal;
	}

	public double getExpectedGoal() {
		return expectedGoal;
	}

	public void setHeuristic(double heuristic) {
		this.heuristic = heuristic;
	}

	public double getHeuristic() {
		return heuristic;
	}

	public void setMaxSteps(int maxSteps) {
		this.maxSteps = maxSteps;
	}

	public int getMaxSteps() {
		return maxSteps;
	}
    
}
