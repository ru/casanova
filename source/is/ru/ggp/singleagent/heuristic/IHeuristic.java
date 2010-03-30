package is.ru.ggp.singleagent.heuristic;
import is.ru.ggp.singleagent.common.ValueNode;

public interface IHeuristic {
    double getHeuristic(ValueNode node);
    String getHeuristicName();
}
