package is.ru.ggp.singleagent.lists;

import org.eclipse.palamedes.gdl.core.model.IGameNode;

/**
 * Interface for the closed list which is used in the A* algorithm.
 */
public interface IClosedList {
	void clearList();
	void addToList(IGameNode node);
	void removeFromList(String stateId);
	boolean contains(IGameNode node);
}
