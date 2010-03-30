package is.ru.ggp.singleagent.heuristic;

public class HeuristicFactory {
    public static IHeuristic getRelaxation(){
        return new RelaxationHeuristic();
    }
}
