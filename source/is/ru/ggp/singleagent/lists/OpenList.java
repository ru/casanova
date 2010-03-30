package is.ru.ggp.singleagent.lists;

import is.ru.ggp.singleagent.common.ValueNode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class OpenList implements IOpenList {


    // Member variables
    private HashMap<String, ValueNode> stateIdHashSet;
    private ArrayList<ValueNode> sortedValueNodeList;

    // Constructor
    public OpenList() {
        this.sortedValueNodeList = new ArrayList<ValueNode>();
        this.stateIdHashSet = new HashMap<String, ValueNode>();
    }

    public void clear() {
        this.stateIdHashSet.clear();
        this.sortedValueNodeList.clear();
    }

    public boolean isEmpty() {
        return this.sortedValueNodeList.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public void add(ValueNode node) {
        this.stateIdHashSet.put(node.getStateId(), node);
        this.sortedValueNodeList.add(node);
        Collections.sort(this.sortedValueNodeList);
        //System.out.println("[Ope] added node");
    }

    public ValueNode get(String stringId) {
        if (this.stateIdHashSet.containsKey(stringId))
            return this.stateIdHashSet.get(stringId);
        return null;
    }

    @Override
    public void reload() {
        Collections.sort(this.sortedValueNodeList);
    }

    public boolean contains(ValueNode node) {
        return this.stateIdHashSet.containsKey(node.getStateId());
    }

    public ValueNode getMostProminentGameNode() {
        // Get the most promising node. It is the first one in the
        // array list where the list is sorted.
        ValueNode returnNode = sortedValueNodeList.get(0);

        // We remove it from the array list.
        sortedValueNodeList.remove(0);

        // We remove it from the hash list.
        this.stateIdHashSet.remove(returnNode.getStateId());

        return returnNode;
    }
    
    
}