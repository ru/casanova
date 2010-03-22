package is.ru.ggp.singleagent.lists;

import is.ru.ggp.singleagent.common.ValueNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class OpenList implements IOpenList{
	

	// Member variables
	// Change this to HashSet.
	private HashMap <String, Object> stateIdHash;
	private ArrayList<ValueNode> sortedValueNodeList;
	
	// Constructor
	public OpenList(){
		this.stateIdHash = new HashMap <String, Object>();
		this.sortedValueNodeList = new ArrayList<ValueNode>();
	}
	
	@Override
	public void clearList() {
		this.stateIdHash.clear();
		this.sortedValueNodeList.clear();
	}
	
	public boolean isEmpty(){
        return this.sortedValueNodeList.isEmpty();
    }

    @SuppressWarnings("unchecked")
	public void add(ValueNode node){
    	this.stateIdHash.put(node.getStateId(), null);
    	this.sortedValueNodeList.add(node);
    	Collections.sort(this.sortedValueNodeList);
    }

	@Override
	public boolean contains(ValueNode node) {
		return this.stateIdHash.containsKey(node.getStateId());
	}

	@Override
	public ValueNode getMostProminentGameNode() {
		// Get the most promising node. It is the first one in the
		// array list where the list is sorted.
		ValueNode returnNode = sortedValueNodeList.get(0);
		
		// We remove it from the array list.
		sortedValueNodeList.remove(0);
		
		// We remove it from the hash list.
		this.stateIdHash.remove(returnNode.getStateId());
		
		return returnNode;
	}
}