package is.ru.ggp.singleagent.common;
import org.eclipse.palamedes.gdl.core.model.IGameNode;

public class ValueNode implements Comparable
{
	// member variable
	public IGameNode gameNode;
		
	public ValueNode(IGameNode gameNode){
		this.gameNode = gameNode;
	}
	
	public int getValue(){
		return this.getDepth() + 0;
	}
	
	public int getDepth(){
		return this.gameNode.getDepth();
	}
	
	public String getStateId(){
		return "";
	}
	
	@Override
	public int compareTo(Object arg0) 
	{
		ValueNode o = (ValueNode)arg0;
		if(this.getValue() < o.getValue())
			return -1;
		if(this.getValue() > o.getValue())
			return 1;
		else
			return 0;
	}
}
