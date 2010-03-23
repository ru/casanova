package is.ru.ggp.singleagent.heuristic;

import is.ru.ggp.singleagent.common.ValueNode;

public class RelaxationHeuristic implements IHeuristic{
    @Override
    public double getHeuristic(ValueNode node) {
        return 0;
    }

    @Override
    public String getHeuristicName() {
        return "Relaxation with penalty";
    }
}
