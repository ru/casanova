package is.ru.ggp.singleagent.lists;
import is.ru.ggp.singleagent.common.ValueNode;

import java.util.HashMap;

public class ClosedList implements IClosedList{
	
	private HashMap <String, ValueNode> hashmap;
	
	public ClosedList(){
		hashmap = new HashMap <String, ValueNode>();
	}
	
	@Override
	public void add(ValueNode node) {
		hashmap.put(node.getStateId(), node);
        //System.out.println("[ClosedList] added node");
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
		return this.hashmap.containsKey(node.getStateId());
	}
}
