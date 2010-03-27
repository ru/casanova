package is.ru.ggp.singleagent.heuristic;

import is.ru.ggp.singleagent.common.ValueNode;
import org.eclipse.palamedes.gdl.core.ast.RuleGoal;
import org.eclipse.palamedes.gdl.core.model.IFluent;
import org.eclipse.palamedes.gdl.core.model.IGame;
import org.eclipse.palamedes.gdl.core.model.utils.Game;
import org.eclipse.palamedes.gdl.core.simulation.Match;
import org.eclipse.palamedes.kif.core.ast.KIFSeq;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RelaxationHeuristic implements IHeuristic{

    private List<String> goalStatePredicates = null;
    private IGame game = null;


    private void findGoalState(Match match){
        this.goalStatePredicates = new LinkedList<String>();
        Game g = (Game)match.getGame();
        KIFSeq<RuleGoal> ff = g.getKB().getGameAST().getRawRuleGoal();

        for(RuleGoal r : ff){
            Pattern p = Pattern.compile(".*goal [a-zA-Z0-9]+ 100.*");
            Matcher matcher = p.matcher(r.toString());

            if (matcher.find()) {
                System.out.println("Generating goal state");

                Pattern functionPattern = Pattern.compile("^[a-zA-Z]*$");

                String[] split = r.toString().split("\n");

                for(int i=1; i<split.length; i++){
                    String s = split[i].replace("\t", "");
                    if(functionPattern.matcher(s).find())
                    {
                        String[] pr = g.getKB().get(s).getImplicationHead().toString().split("\n");
                        for(int j = 1; j<pr.length; j++)
                        {
                            s = pr[j].replace("\t", "");
                            s = s.replace("(true ", "");
                            s = s.substring(0, s.length()-1);
                            if(s.endsWith("))"))
                                s = s.substring(0, s.length()-2);
                            this.goalStatePredicates.add(s);
                        }

                    }
                    else{
                        if(s.startsWith("(true "))
                        {
                            s = s.replace("(true ", "");
                            s = s.substring(0, s.length()-1);

                            if(s.endsWith("))"))
                                s = s.substring(0, s.length()-1);
                            this.goalStatePredicates.add(s);
                        }
                        else{
                            System.out.println("Error, we must consider the predicate " + s);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void readGoalStateFromMatch(Match match) {
        this.findGoalState(match);
    }

    /**
     * Constructor for the Relaxation heuristic
     * class.
     * @param game Accepts instance of IGame.
     */
    public RelaxationHeuristic(IGame game){
        this.game = game;
    }


    @Override
    public double getHeuristic(ValueNode node) {
        System.out.println("[A*] calculating heuristic for state");

        // Calculating unsatisfied goals.
        int unsatisfiedGoals = 0;

        for(IFluent f : node.gameNode.getState().getFluents()){
            if(!this.goalStatePredicates.contains(f.toString())){
                unsatisfiedGoals += 1;
            }
        }

        // Distance calculated.
        return unsatisfiedGoals;
    }

    @Override
    public String getHeuristicName() {
        return "Relaxation with penalty";
    }   
}
