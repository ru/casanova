package is.ru.ggp.singleagent.heuristic;
import is.ru.ggp.singleagent.common.ValueNode;
import org.eclipse.palamedes.gdl.core.simulation.Match;

import java.util.List;

public interface IHeuristic {
    double getHeuristic(ValueNode node);
    String getHeuristicName();
    void readGoalStateFromMatch(Match match);
}
