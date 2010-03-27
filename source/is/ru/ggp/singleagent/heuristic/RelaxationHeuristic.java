package is.ru.ggp.singleagent.heuristic;

import is.ru.ggp.singleagent.common.ValueNode;
import org.eclipse.palamedes.gdl.core.model.IFluent;
import org.eclipse.palamedes.gdl.core.model.IGame;
import java.util.List;

public class RelaxationHeuristic implements IHeuristic{

    private IGame game;

    public RelaxationHeuristic(IGame game){
        this.game = game;
    }


    @Override
    public double getHeuristic(ValueNode node) {
        return 0;
    }

    @Override
    public String getHeuristicName() {
        return "Relaxation with penalty";
    }
}
