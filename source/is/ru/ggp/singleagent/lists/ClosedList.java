package is.ru.ggp.singleagent.lists;
import java.util.HashMap;
import java.util.Random;

import org.eclipse.palamedes.gdl.core.model.IGameNode;

public class ClosedList implements IClosedList{
	
	private HashMap <String, IGameNode> hashmap;
	
	public ClosedList(){
		hashmap = new HashMap <String, IGameNode>();
	}

	private String createStateId (final IGameNode node){
		Random randomGenerator = new Random();
		int randomInt = randomGenerator.nextInt(Integer.MAX_VALUE);
		return Integer.toString(node.hashCode() + randomInt);
	}
	
	@Override
	public void addToList(IGameNode node) {
		hashmap.put(createStateId(node), node);
	}

	@Override
	public void clearList() {
		hashmap.clear();	
	}

	@Override
	public void removeFromList(String stateId) {
		hashmap.remove(stateId);
	}

	@Override
	public boolean contains(IGameNode node) {
		return hashmap.containsValue(node);
	}

}