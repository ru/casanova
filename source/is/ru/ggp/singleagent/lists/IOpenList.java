package is.ru.ggp.singleagent.lists;

import is.ru.ggp.singleagent.common.ValueNode;
/**
 * Interface for the Open list which is used in the A* algorithm.
 */
public interface IOpenList {
	void clearList();	
	boolean contains(ValueNode node);
	ValueNode getMostProminentGameNode();
	boolean isEmpty();
    void add(ValueNode node);
    ValueNode get(String stringId);
}
