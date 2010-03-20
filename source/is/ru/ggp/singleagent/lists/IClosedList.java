package is.ru.ggp.singleagent.lists;

/**
 * Interface for the closed list which is used in the A* algorithm.
 */
public interface IClosedList {
	void clearList();
	void addToList(String stateId);
	void removeFromList(String stateId);
}
