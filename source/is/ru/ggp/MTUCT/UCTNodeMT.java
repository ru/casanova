package is.ru.ggp.MTUCT;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.palamedes.gdl.core.model.IGameState;
import org.eclipse.palamedes.gdl.core.model.IMove;
import org.eclipse.palamedes.gdl.core.model.IReasoner;
import org.eclipse.palamedes.gdl.core.model.IGame;

public class UCTNodeMT
{
	static IReasoner myReasoner = null;
    static String[] rolenames;
	IGameState state;
	UCTNodeMT parent;//if parent == null, then this == root
	UCTNodeMT[] children;
	double[][] aggregate;
	int[][] countVisited;
	int visits;
	IMove[][] moves;
	int[] parentpointer;


	public UCTNodeMT(IGameState s, UCTNodeMT p, int[] ppoint) {
		state = s;
		parent = p;
		visits = 0;
		parentpointer = ppoint;
		//moves = s.getLegalMoves();
        moves =  new IMove[rolenames.length][];
        int rolecounter = 0;
        for(String role:rolenames)
        {
            try {
                moves[rolecounter] = myReasoner.getLegalMoves(role,s);
            } catch (InterruptedException e) {
                 System.out.println("constructor");
                 e.printStackTrace();}
            rolecounter++; 
        }
		int movesIndex = 0;
		//Random myRandom = new Random();
		//IMove[] firstSimulation = new IMove[moves.length];
        //System.out.println("moves.length:"+moves.length);
		/*for(IMove[] playermoves: moves)
        {
            for(IMove move: playermoves)
            {
                      System.out.println("Found move:"+move.getMove());
            }

        }  */
        aggregate = new double[moves.length][];
        countVisited = new int[moves.length][];
        rolecounter = 0;
        int countMoves = 0;
		for(IMove[] playersMoves : moves)
		{
            //System.out.println("#"+movesIndex+" : "+moves.length);
			aggregate[movesIndex] = new double[moves[movesIndex].length];
			countVisited[movesIndex] = new int[moves[movesIndex].length];
			for(int i = 0; i < moves[movesIndex].length;i++)
			{
				aggregate[movesIndex][i] = 0.0d;
				countVisited[movesIndex][i] = 0;
                countMoves++;
			}
            /*System.out.print("Constructor found ");
            for(IMove move:playersMoves)
                System.out.print(":"+move.toString());
            System.out.println(" for "+rolenames[rolecounter]);*/
			//children[countVisited] = new UCTNode[moves.length];
			//firstSimulation[countVisited] = playersMoves[myRandom.nextInt(playersMoves.length-1)];
			movesIndex++;
		}
		children = new UCTNodeMT[countMoves+1];

		//muna: java er pass-reference-by-value þannig að þú getur gert parameter.changeSomething()
		//		en EKKI parameter = new Something()

	}

	public int[] simulate(int[] moveIndex, int maxDepth)
	{
		IGameState newState = null;
        IGameState simulState = null;
		Random myRandom = new Random();
        int[] gvalues = new int[rolenames.length];
        /*for(int result:moveIndex)
            System.out.println("moveindex:"+result);*/
		synchronized(this)
		{
			IMove[] move = new IMove[moves.length];
			int singledimension = 0;
			int rand = 0;
			for(int i=0 ; i < moves.length ; i++)
			{
				//rand = myRandom.nextInt(moves[i].length);
				move[i] = moves[i][moveIndex[i]];
			}
            try{
			    newState = myReasoner.getNextState(state,move);
                simulState = myReasoner.getNextState(state,move);
            } catch(InterruptedException e)

            {
                 System.out.println("make first move");
                 e.printStackTrace();
            }
			children[movesToChildrenIndex(moveIndex)] = new UCTNodeMT(newState,this,moveIndex);
		}

		IMove[] randomMove = new IMove[rolenames.length];
        IMove[] playermovelist;
		//List<IMove[]> simulMoves;
        simulate:
		while(maxDepth != 0)
		{

			/*simulMoves = simulState.getCombinedLegalMoves();
			randomMove = (IMove[])(simulMoves.get(myRandom.nextInt(simulMoves.size())));*/
            int counter = 0;
            //System.out.println("Simulating, nodes till termination:"+maxDepth);
            try{
                if (simulState.isTerminal())
                {
                    gvalues = goalvalues(simulState);
                    //System.out.println("Terminal state with goalvalues:"+Arrays.toString(gvalues));
                    break simulate;
                }
                for(String role: rolenames)
                {
                    playermovelist = myReasoner.getLegalMoves(role,simulState);
                    randomMove[counter] = playermovelist[myRandom.nextInt(playermovelist.length)];
                    //System.out.println("Random move:"+randomMove[counter].toString());
                    counter++;
                }
			    simulState = myReasoner.getNextState(simulState,randomMove);
                maxDepth--;
                //System.out.println(simulState.toString());
                /*for(int value:simulState.getGoalValues())
                    System.out.println("Value:"+value);*/
            }
            catch(InterruptedException e)
            {
                System.out.println("simulate");
                e.printStackTrace();
            }
		}
        /*System.out.print("Goalvalues");
        for(int value:gvalues)
            System.out.print(":"+value);
        System.out.println();*/
        //System.out.println("Terminating simulation, maxdepth "+maxDepth);
		return gvalues;
	}
    public void visit(int[] choices)
    {
        synchronized(this)
        {
            for(int i = 0; i < choices.length;i++)
                countVisited[i][choices[i]]++;
            visits++;
        }
    }

	public void update(int[] moveToUpdate, int[] values)
	{
		synchronized(this)
		{
            /*int counter = 0;
            for(int value: moveToUpdate)
            {
                System.out.println("Update move:"+value+" with value:"+values[counter]);
                counter++;
            }     */
			for(int i = 0; i < moveToUpdate.length; i++)
				aggregate[i][moveToUpdate[i]] += (double)values[i];
		}
	}

	public int[] evaluate(double c)
	{
        //System.out.println("Asked to evalute, I use:"+Arrays.toString(countVisited)+" for visits,"+Arrays.toString(aggregate)+" as gvalues. "+"Total visits:"+visits);
		int[] maxvalueindexes;
		synchronized(this)
		{
			int moveCount = 0, maxvalueindex = 0;
			double mc_score = 0.0d,uct_max = 0.0d, c_v = 0.0d;
			maxvalueindexes = new int[moves.length];
			for(int player = 0; player < moves.length;player++)
			{
                moveCount = 0;
                uct_max = 0.0d;
				for(double moveRank : aggregate[player])
				{
					c_v = (double)countVisited[player][moveCount];
					if(countVisited[player][moveCount] == 0)
						mc_score = c;
					else
					{
						mc_score = moveRank/c_v;
						mc_score += c*(Math.sqrt(Math.log(((double)visits)/c_v)));
					}
					if(mc_score > uct_max)
					{
						uct_max = mc_score;
						maxvalueindex = moveCount;
					}
                    moveCount++;
				}
                //System.out.println("UCT score:"+uct_max+" move:"+maxvalueindex);
				maxvalueindexes[player] = maxvalueindex;
			}
		}
        //for(int i = 0; i < maxvalueindexes.length;i++ )
        //    System.out.println("Chose:"+moves[i][maxvalueindexes[i]].getMove());
		return maxvalueindexes;
	}

	public int movesToChildrenIndex(int[] moveIndexes)
	{
		int playercount = 0;
		int multiplyby = 1;
		int returnvalue = 0;
		for(int ind: moveIndexes)
		{
			returnvalue += ind*multiplyby;
			multiplyby = multiplyby * moves[playercount].length;
			playercount++;
		}
		return returnvalue;
	}

    public IMove getBest(String playerstring)
    {
        int bestScore = 0;
        int bestIndex = 0;
        int player = 0,pcount = 0;
        for(String r:rolenames)
        {
            if(r.compareTo(playerstring) == 0)
            {
                player = pcount;
                break;
            }
            pcount++;
        }
        for(int i = 0; i < moves[player].length;i++)
        {
              if(countVisited[player][i]>bestScore)
              {bestScore = countVisited[player][i]; bestIndex = i;}
        }
        return moves[player][bestIndex];
    }
    public UCTNodeMT progress(String[] newMove)
    {
        int moveCount;
        int playercount = 0;
        int[] milkCarton = new int[moves.length];
        for(String move : newMove)
        {
            moveCount = 0;
            for(IMove myMove: moves[playercount])
            {
                if(move.equals(myMove.getMove()))
                {
                    milkCarton[playercount] =  moveCount;
                    break;
                }
                moveCount++;
            }
            playercount++;
        }
        return children[movesToChildrenIndex(milkCarton)];
    }

    public int[] goalvalues(IGameState s) throws InterruptedException
    {
        int[] values = new int[rolenames.length];
        for(int i = 0; rolenames.length>i;i++)
            values[i]=myReasoner.getGoalValue(rolenames[i],s);
        return values;
    }

	static public void setReasoner(IGame game)
	{
		myReasoner = game.getReasoner();
        rolenames = game.getRoleNames();
	}

}