package de.tudresden.inf.ggp.basicplayer;

import org.eclipse.palamedes.gdl.core.simulation.StrategyFactory;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.palamedes.gdl.connection.Message;
import org.eclipse.palamedes.gdl.connection.Player;
import org.eclipse.palamedes.gdl.connection.PlayerServer;
import org.eclipse.palamedes.gdl.core.model.GameFactory;
import org.eclipse.palamedes.gdl.core.model.IGame;
import org.eclipse.palamedes.gdl.core.model.utils.Game;
import org.eclipse.palamedes.gdl.core.simulation.IStrategy;
import is.ru.ggp.singleagent.AStarStategy;
public final class MyPlayer extends Player {

	static {
		StrategyFactory.getInstance().addDescription(
							"AStarStrategy",
							AStarStategy.class.getCanonicalName(),
            				"Simulates games and chooses the best path." );
	}
	
	
    /**
     * This method is called when a new match begins.
	 *
     * <br/>
     * msg="(START MATCHID ROLE GAMEDESCRIPTION STARTCLOCK PLAYCLOCK)"<br/>
     * e.g. msg="(START tictactoe1 white ((role white) (role black) ...) 1800 120)" means:
     * <ul>
     *   <li>the current match is called "tictactoe1"</li>
     *   <li>your role is "white",</li>
     *   <li>
     *     after at most 1800 seconds, you have to return from the
     *     commandStart method
     *   </li>
     *   <li>for each move you have 120 seconds</li>
     * </ul>
     */
    public void commandStart(Message msg){

        // create the clock
        int playClock  = msg.getPlayClock()  - 1;
        int startClock = msg.getStartClock() - 1;

        System.out.println( "Start Clock " + startClock );
        System.out.println( "Play  Clock " + playClock  );

        // get the game from the database
        /** XXX: You can change here between GameFactory.JAVAPROVER, GameFactory.JOCULAR
         *       and GameFactory.PROLOG. Both Stanford resolvers have some
         *       drawbacks. Currently JAVAPROVER is slower whereas JOCULAR
         *       does not support the full range of special characters like '+'.
         *       GameFactory.PROLOG is probably the fastest option, but you need
         *       to have Eclipse-Prolog installed (http://www.eclipse-clp.org/). */
        GameFactory factory 	= GameFactory.getInstance();
        IGame 		runningGame = factory.createGame( GameFactory.JAVAPROVER,
        											  msg.getGameDescription() );
        System.out.println("MyPlayer created the game.");

        
        /** XXX: If you implement another strategy here is the place to instantiate it */
        IStrategy strategy = StrategyFactory.getInstance().createStrategy("AStarStrategy");

        System.out.println( "MyPlayer created the strategy "      +
                            strategy.getClass().getSimpleName() +
                            "." );

        System.out.println( "MyPlayer starts contemplate while doing yoga." );

        // create a match
        realMatch = createRealMatch( msg.getMatchId(),
                                   	 runningGame,
                                   	 strategy,
                                   	 msg.getRole(),
                                   	 startClock,
                                   	 playClock );
        System.out.println( "MyPlayer created the match." );
        System.out.println( "MyPlayer is prepared to start the game." );
        System.out.println("stats:"+runningGame.getStatistic());
    }
    

    /**
     * This method is called once for each move<br/>
     * <br/>
     * msg = "(PLAY MATCHID JOINTMOVE)<br/>
     * JOINTMOVE will be NIL for the first PLAY message and the list of the
     * moves of all the players in the previous state<br/>
     * e.g. msg="(PLAY tictactoe1 NIL)" for the first PLAY message
     *   or msg="(PLAY tictactoe1 ((MARK 1 2) NOOP))" if white marked cell (1,2)
     *   and black did a "noop".<br/>
     * @return the move of this player
     */
    public String commandPlay(Message msg){
        checkMatchId(msg);

        // only make a turn if the move list is not empty
        // is it empty it means we hit the first play message, setting the
        // initial state is done while constructing the match object
        if ( msg.hasMoves() ){
            System.out.println( "Moves from GameMaster: " +
                                Arrays.toString(msg.getMoves()) );

            String[] prepared = prepareMoves(msg);

            // got the initial NIL in case of length == 0
            if (prepared.length != 0)

                // prepare moves
                realMatch.makeTurn( prepared );

        }

        // real work is done here
        String move = realMatch.getMove().getMove().toUpperCase();

        // logging the resulting move
        System.out.println( "Our move: " + move );
        System.out.println("stats:"+((Game)realMatch.getGame()).getStatistic());
        return move;
    }


    /**
     * This method is called if the match is over
     *
     * msg="(STOP MATCHID JOINTMOVE)
     */
    public void commandStop(Message msg){
        checkMatchId(msg);

        // adds the moves to the match
        if ( msg.hasMoves() )

            // real work is done here
            realMatch.makeTurn( prepareMoves(msg) );

        // check if we agree to be in a final state and log the result
        if ( !realMatch.getCurrentNode().isTerminal() )
            System.out.println( "Game stopped but not finished" );
        else{
            System.out.println( "Game finished" );
			try {
	        	int[] goalvalues;
				goalvalues = realMatch.getGame().getGoalValues(realMatch.getCurrentNode());
	        	System.out.println( "goal:" );
	        	for(int i=0;i<goalvalues.length;++i){
	        		System.out.print( goalvalues[i]+" " );
	        	}
	        	System.out.println();
			} catch (InterruptedException e) {
				System.out.println("Timeout while computing goal values:");
				e.printStackTrace();
			}
        }
    }

    /**
     * starts the game player and waits for messages from the game master <br>
     * Command line options: --port=<port> --slave=<true|false>
     */
    public static void main(String[] args){
         
        /* create and start player server */
    	try {
    		new PlayerServer( new MyPlayer(), 
    						  PlayerServer.getOptions(args) ).waitForExit();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}
