package is.ru.ggp.singleagent.lists;
import is.ru.ggp.singleagent.common.ValueNode;

import java.util.HashMap;
import java.util.Random;

import org.eclipse.palamedes.gdl.core.model.IGameNode;

public class ClosedList implements IClosedList{
	
	private HashMap <String, ValueNode> hashmap;
	
	public ClosedList(){
		hashmap = new HashMap <String, ValueNode>();
	}
	
	@Override
	public void addToList(ValueNode node) {
		hashmap.put(node.getStateId(), node);
	}

	@Override
	public void clearList() {
		hashmap.clear();	
	}

	@Override
	public void removeFromList(String stateId) {
		if(this.hashmap.containsKey(stateId))
			hashmap.remove(stateId);
	}

	@Override
	public boolean contains(ValueNode node) {
		return false;
	}
}
