package is.ru.ggp.singleagent.common;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.eclipse.palamedes.gdl.core.model.IFluent;
import org.eclipse.palamedes.gdl.core.model.IGame;
import org.eclipse.palamedes.gdl.core.model.IGameNode;
import org.eclipse.palamedes.gdl.core.model.IMove;

public class ValueNode implements Comparable {
    // member variable
    public IGameNode gameNode;

    public double h = 0;
    public double g = 0;
    public ValueNode parent = null;
    public IMove parentAction = null;
    public IGame game;

    public ValueNode(IGameNode gameNode, IGame game) {
        this.gameNode = gameNode;
        this.game = game;
    }

    public String getStateId() {
        return this.gameNode.getState().toString();
    }

    @Override
    public int compareTo(Object arg0) {
        ValueNode o = (ValueNode) arg0;
        if (this.h + this.g < o.h + this.g)
            return -1;
        if (this.h + this.g > o.h + this.g)
            return 1;
        else
            return 0;
    }

    public int getGoalValue() {

        try {
            int goal = game.getReasoner().getGoalValue(game.getRoleNames()[0], this.gameNode.getState());
            return goal;
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return 0;
        }
    }

    public List<String> getpredicates()
    {
        LinkedList<String> returnList = new LinkedList<String>();

        for(IFluent s : this.gameNode.getState().getFluents())
            returnList.add(s.toString());

        return returnList;

    }
}
