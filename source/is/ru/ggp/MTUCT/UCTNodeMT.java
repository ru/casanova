package is.ru.ggp.MTUCT;

import java.util.List;
import java.util.Random;

import org.eclipse.palamedes.gdl.core.model.IGameState;
import org.eclipse.palamedes.gdl.core.model.IMove;
import org.eclipse.palamedes.gdl.core.model.IReasoner;
import org.eclipse.palamedes.gdl.core.model.IGame;

public class UCTNodeMT
{
	static IReasoner myReasoner = null;
	IGameState state;
	UCTNodeMT parent;//if parent == null, then this == root
	UCTNodeMT[] children;
	double[][] aggregate;
	int[][] countVisited;
	int visits;
	IMove[][] moves;
	int[] parentpointer;

	public UCTNodeMT(IGameState s, UCTNodeMT p, int[] ppoint)
	{
		state = s;
		parent = p;
		visits = 0;
		parentpointer = ppoint;
		moves = s.getLegalMoves();
		int movesIndex = 0;
		//Random myRandom = new Random();
		//IMove[] firstSimulation = new IMove[moves.length];
		aggregate = new double[moves.length][];
		for(IMove[] playersMoves : moves)
		{
			aggregate[movesIndex] = new double[moves[movesIndex].length];
			countVisited[movesIndex] = new int[moves[movesIndex].length];
			for(int i = 0; i < moves.length;i++)
			{
				aggregate[movesIndex][i] = 0.0d;
				countVisited[movesIndex][i] = 0;
			}
			//children[countVisited] = new UCTNode[moves.length];
			//firstSimulation[countVisited] = playersMoves[myRandom.nextInt(playersMoves.length-1)];
			movesIndex++;
		}
		children = new UCTNodeMT[movesIndex+1];

		//muna: java er pass-reference-by-value þannig að þú getur gert parameter.changeSomething()
		//		en EKKI parameter = new Something()

	}

	public int[] simulate(int[] moveIndex, int maxDepth)
	{
		IGameState newState = state;
		Random myRandom = new Random();
		synchronized(children)
		{
			IMove[] move = new IMove[moves.length];
			int singledimension = 0;
			int rand = 0;
			for(int i=0 ; i < moves.length ; i++)
			{
				rand = myRandom.nextInt(moves[i].length-1);
				move[i] = moves[i][rand];
			}
            try{
			newState = myReasoner.getNextState(state,move);   } catch(Exception e){}
			children[movesToChildrenIndex(moveIndex)] = new UCTNodeMT(newState,this,moveIndex);
		}


		IGameState simulState = newState;

		IMove[] randomMove;
		List simulMoves;
		while(maxDepth != 0)
		{
			if (simulState.isTerminal())
				break;
			simulMoves = simulState.getCombinedLegalMoves();
			randomMove = (IMove[])(simulMoves.get(myRandom.nextInt(simulMoves.size()-1)));
            try{
			    simulState = myReasoner.getNextState(simulState,randomMove);
            }
            catch(Exception e)
            {

            }
		}

		return simulState.getGoalValues();
	}

	public void update(int[] moveToUpdate, int[] values)
	{
		synchronized(aggregate)
		{
			for(int i = 0; i < moveToUpdate.length; i++)
				aggregate[i][moveToUpdate[i]] += (double)values[i];
		}
	}

	public int[] evaluate(double c)
	{
		int[] maxvalueindexes;
		synchronized(this)
		{
			int moveCount = 0, maxvalueindex = 0;
			double mc_score = 0.0d,uct_max = 0.0d, c_v = 0.0d;
			maxvalueindexes = new int[moves.length];
			for(int player = 0; player < moves.length;player++)
			{
				for(double moveRank : aggregate[player])
				{
					c_v = (double)countVisited[player][moveCount];
					if(countVisited[player][moveCount] == 0)
						mc_score = 0.0d;
					else
					{
						mc_score = aggregate[player][moveCount]/c_v;
						mc_score+=c*(Math.sqrt(Math.log((double)visits)/c_v));
					}
					if(mc_score > uct_max)
					{
						uct_max = mc_score;
						maxvalueindex = moveCount;
					}
				}
				maxvalueindexes[player] = maxvalueindex;
				countVisited[player][maxvalueindex]++;
			}
			visits++;

		}
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

    public IMove getBest(int player)
    {
        int bestScore = 0;
        int bestIndex = 0;
        for(int i = 0; i < moves[player].length;i++)
        {
              if(countVisited[player][i]>bestScore)
              {bestScore = countVisited[player][i]; bestIndex = i;}
        }
        return moves[player][bestIndex];
    }
    public UCTNodeMT progress(IMove[] newMove)
    {
        int moveCount;
        int playercount = 0;
        int[] milkCarton = new int[moves.length];
        for(IMove move : newMove)
        {
            moveCount = 0;
            for(IMove myMove: moves[playercount])
            {
                if(move.getMove().equals(myMove.getMove()))
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
	static public void setReasoner(IGame game)
	{
		myReasoner = game.getReasoner();
	}

}