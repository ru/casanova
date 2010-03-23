package is.ru.ggp.singleagent.lists;

import is.ru.ggp.singleagent.common.ValueNode;

import org.eclipse.palamedes.gdl.core.model.IGameNode;

/**
 * Interface for the closed list which is used in the A* algorithm.
 */
public interface IClosedList {
	void clearList();
	void add(ValueNode node);
	void removeFromList(String stateId);
	boolean contains(ValueNode node);
}
