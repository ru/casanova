package is.ru.ggp.MTUCT;

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
        UCTNodeMT next;
        int[] nextnode;
        int[] results;
        while (true) {
            nextnode = current.evaluate(42.0d);
            next = current.children[current.movesToChildrenIndex(nextnode)];
            if (next == null)
                break;
            else
                current = next;
        }
        results = current.simulate(nextnode, maxDepth);
        while (true) {
            current.update(results, nextnode);
            nextnode = current.parentpointer;
            current = current.parent;
            if (current == null)
                break;
        }
        int throwaway = counter.decrementAndGet();
    }
}