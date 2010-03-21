package is.ru.ggp.singleagent.lists;

import org.eclipse.palamedes.gdl.core.model.IGameNode;

/**
 * Interface for the Open list which is used in the A* algorithm.
 */
public interface IOpenList {
	void clearList();
	IGameNode getGameNode(String stateId);
	IGameNode getMostProminentGameNode();
    boolean isEmpty();
    void add(IGameNode node);
}
