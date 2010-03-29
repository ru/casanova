package is.ru.ggp.MTUCT;

import org.eclipse.palamedes.gdl.core.model.IGameState;
import org.eclipse.palamedes.gdl.core.model.IGame;
import org.eclipse.palamedes.gdl.core.model.IMove;

public class UCTStrategyMultiThread implements Runnable
{
	private UCTNodeMT root;
	IGame myGame;
	int maxDepth;
    Integer threadCount;
    Integer maxThreads;
    long timeToSleep;

	public UCTStrategyMultiThread(IGame myGame, IGameState startSearch, int maxDepth, int numThreads, long tts)
	{
		this.myGame = myGame;
		this.maxDepth = maxDepth;
		UCTNode.setReasoner(myGame);
		root = new UCTNodeMT(startSearch,null,null);
        threadCount = new Integer(0);
        maxThreads = new Integer(numThreads);
        timeToSleep = tts;
	}
    public IMove getBestMove(int player)
    {
        return root.getBest(player);
    }
    public void setThreads(int max)
    {
        maxThreads = new Integer(max);   
    }
    public void run()
    {
        while(true)
        {
            synchronized(threadCount)
            {
                while(threadCount<maxThreads)
                {
                    threadCount++;
                    UCTThread newThread = new UCTThread(root,maxDepth);
                    new Thread(newThread).start();
                }
            }
            try{
            Thread.sleep(timeToSleep);  }catch(InterruptedException e){}
        }
    }
    public void advanceRoot(IMove[] validated)
    {
        int mtholder = maxThreads;
        maxThreads = 0;
        while(0 < threadCount)
        {
                  try{
            Thread.sleep(timeToSleep);  }catch(InterruptedException e){}
        }
        root = root.progress(validated);
        root.parent = null;
        root.parentpointer = null;
        maxThreads = mtholder;
        System.gc();  
    }
}