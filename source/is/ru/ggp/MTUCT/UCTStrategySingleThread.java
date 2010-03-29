package is.ru.ggp.MTUCT;

import org.eclipse.palamedes.gdl.core.model.IGameState;
import org.eclipse.palamedes.gdl.core.model.IGame;
import org.eclipse.palamedes.gdl.core.model.IMove;

public class UCTStrategySingleThread
{
	private UCTNode root;
	IGame myGame;
	int maxDepth;
	public UCTStrategySingleThread(IGame myGame, IGameState startSearch, int maxDepth)
	{
		this.myGame = myGame;
		this.maxDepth = maxDepth;
		UCTNode.setReasoner(myGame);
		root = new UCTNode(startSearch,null,null);

	}
    public IMove getBestMove(int player)
    {
        return root.getBest(player);
    }

    public void advanceRoot(IMove[] validated)
    {
        root = root.progress(validated);
        System.gc();
    }
	
	public void search(long timeToSearch, int roleIndex)
	{
		long startTime = java.lang.System.currentTimeMillis();
		UCTNode current = root;
		UCTNode next;
		int[] nextnode;
		int[] results;
		while(startTime + timeToSearch > java.lang.System.currentTimeMillis())
		{
			while(true)
			{
				nextnode = current.evaluate(42.0d);
				next = current.children[current.movesToChildrenIndex(nextnode)];
				if(next == null)
					break;
				else
					current = next;
			}
			results = current.simulate(nextnode,maxDepth);
			while(true)
			{
				current.update(results,nextnode);
				nextnode = current.parentpointer;
				current = current.parent;
				if(current == null)
					break;
			}
		}
	}
}