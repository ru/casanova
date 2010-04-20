package is.ru.ggp.MTUCT;

import org.eclipse.palamedes.gdl.core.model.IMove;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class UCTThread implements Runnable{
    private UCTNodeMT root;
    int maxDepth;
    AtomicInteger counter;

    public UCTThread(UCTNodeMT root, int maxD, AtomicInteger c)
    {
        this.root = root;
        this.maxDepth = maxD;
        counter = c;
    }

    public void run()
    {
        UCTNodeMT current = root;
        UCTNodeMT next = null;
        int[] nextnode;
        int[] results = null;

        while (true) {
            nextnode = current.evaluate(42.0d);
            current.visit(nextnode);
            //System.out.println("Selecting next node:"+ Arrays.toString(nextnode)+" which would be the "+current.movesToChildrenIndex(nextnode)+"th child");
            //for(IMove printMove: current.moves[0])
            //    System.out.println(printMove.getMove());

            next = current.children[current.movesToChildrenIndex(nextnode)];

            if (next == null)
            {
                //System.out.println("Found leaf, simulating from "+current.state.toString());
                results = current.simulate(nextnode, maxDepth);
                break;
            }
            else
            {
                if(current.state.isTerminal())
                {
                    //System.out.println("State is terminal, returning goalvalues");
                    try
                    {
                        results = current.goalvalues(current.state);
                    } catch (InterruptedException e)
                    {
                        //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    break;
                }
                current = next;
            }
        }


        while (true) {
            //System.out.println("updating");
            //if(results == null)
            //     System.out.println("Results are null");
            current.update(nextnode,results);
            nextnode = current.parentpointer;
            current = current.parent;
            if (current == null)
                break;
        }
        int throwaway = counter.decrementAndGet();
    }
}
