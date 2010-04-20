package is.ru.ggp.MTUCT;

import org.eclipse.palamedes.gdl.core.model.IGameNode;
import org.eclipse.palamedes.gdl.core.model.IGameState;
import org.eclipse.palamedes.gdl.core.model.IGame;
import org.eclipse.palamedes.gdl.core.model.IMove;
import org.eclipse.palamedes.gdl.core.simulation.strategies.AbstractStrategy;

import java.util.concurrent.atomic.AtomicInteger;

public class UCTStrategyMultiThread extends AbstractStrategy implements Runnable
{
	private UCTNodeMT root;
	IGame myGame;
	int maxDepth;
    AtomicInteger threadCount;
    Integer maxThreads;
    long timeToSleep;
    String myRole;
    boolean stillRunning;

	public UCTStrategyMultiThread( IGame myGame, IGameState startSearch, int maxDepth, int numThreads, long tts)
	{
		this.myGame = myGame;
		this.maxDepth = maxDepth;
		UCTNodeMT.setReasoner(myGame);
		root = new UCTNodeMT(startSearch,null,null);
        threadCount = new AtomicInteger(0);
        maxThreads = new Integer(numThreads);
        timeToSleep = tts;
        stillRunning = false;
	}
    public void setRole(String role)
    {
       myRole = role; 
    }
    public IMove getBestMove(String player)
    {
        return root.getBest(player);
    }
    public void setThreads(int max)
    {
        maxThreads = new Integer(max);   
    }
    public void stop()
    {
        stillRunning = false;
    }
    public void run()
    {
        stillRunning = true;
        while(stillRunning)
        {

                while(threadCount.getAndIncrement()<maxThreads)
                {
                    UCTThread newThread = new UCTThread(root,maxDepth,threadCount);
                    new Thread(newThread).start();
                }
                int throwaway = threadCount.getAndDecrement();
            try{
            Thread.sleep(0);  }catch(InterruptedException e){}
        }
    }
    public void advanceRoot(String[] validated) throws    InterruptedException
    {
        int mtholder = maxThreads;
        maxThreads = 0;
        while(0 < threadCount.get())
        {
            Thread.sleep(0);
        }
        root = root.progress(validated);
        root.parent = null;
        root.parentpointer = null;
        maxThreads = mtholder;
        System.gc();  
    }

    @Override
    public IMove getMove(IGameNode iGameNode) {
        return getBestMove(myRole);
    }
}