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
	
	public int getHauristicValue(){
		return this.getDepth() + 0;
	}
	
	private int getDepth(){
		return this.gameNode.getDepth();
	}
	
	public String getStateId(){
		return this.createStateId();
	}
	
	@Override
	public int compareTo(Object arg0) 
	{
		ValueNode o = (ValueNode)arg0;
		if(this.getHauristicValue() < o.getHauristicValue())
			return -1;
		if(this.getHauristicValue() > o.getHauristicValue())
			return 1;
		else
			return 0;
	}
	
	private String createStateId(){		
		return Integer.toString(this.hashCode());
	}
	
}