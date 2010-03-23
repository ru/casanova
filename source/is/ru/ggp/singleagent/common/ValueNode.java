package is.ru.ggp.singleagent.common;
import java.util.Random;

import org.eclipse.palamedes.gdl.core.model.IGameNode;
import org.eclipse.palamedes.gdl.core.model.IMove;

public class ValueNode implements Comparable
{
	// member variable
	public IGameNode gameNode;

    public double h = 0;
    public double g = 0;
    public ValueNode parent = null;
    public IMove parentAction = null;
		
	public ValueNode(IGameNode gameNode){
		this.gameNode = gameNode;
	}
    
	public String getStateId(){
        return this.gameNode.getState().toString();
	}
	
	@Override
	public int compareTo(Object arg0) {
		ValueNode o = (ValueNode)arg0;
		if(this.h + this.g < o.h + this.g)
			return -1;
		if(this.h + this.g > o.h + this.g)
			return 1;
		else
			return 0;
	}

    public int getGoalValue(){
        return this.gameNode.getState().getGoalValue(0);
    }
}
