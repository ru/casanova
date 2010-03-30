package is.ru.ggp.statehashing;
import org.eclipse.palamedes.gdl.core.model.IFluent;
import org.eclipse.palamedes.gdl.core.model.IGameState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ellioman
 * Date: Mar 18, 2010
 * Time: 9:16:54 PM
 */
public class StateHash
{
    private Map<Integer,Integer> m_compoundHashMap = new HashMap<Integer,Integer>();
    private List<IFluent> m_compoundVector = new ArrayList<IFluent>();

    /*
     * Singleton implementation.
     */
    private StateHash() {}

    private static class stateHolder {
        private static final StateHash INSTANCE = new StateHash();
    }

    public static StateHash getInstance() {
        return stateHolder.INSTANCE;
    }

    
    /*
     * @return the array of IFluents represented by the int array 
     */
    public IFluent[] pullState(int[] hashKey)
    {
        int index = 0;
        IFluent[] tempString = new IFluent[hashKey.length];
        
        // Find the compound sentences in the vector
        for(int i=0; i<hashKey.length; i++) {
            if (hashKey[i] == 1) {
                tempString[index] = m_compoundVector.get(i);
                index++;
            }
        }

        // trim the array
        IFluent[] trimmed = new IFluent[index];
        System.arraycopy(tempString, 0, trimmed, 0, index);

        return trimmed;
    }

    /*
     *
     */
    public int[] pushState(IGameState state)
    {
        List<String> fluentNames = state.getFluentNames();
        List<IFluent> fluents = state.getFluents();
        int[] stateID = new int[fluentNames.size() + m_compoundVector.size()];

        int index = 0;
        
        /* Create a state array */
        // For each string in the array...
        for (IFluent c : fluents) {

            int c_hash = c.hashCode();
            // If the compound is already in the vector..
            if (m_compoundHashMap.containsKey(c_hash)) {
                index = m_compoundHashMap.get(c_hash);
            }

            // If the compound is not in the vector...
            else {
                index = m_compoundVector.size();
                m_compoundVector.add(c);
                m_compoundHashMap.put(c_hash, index);
            }

            // Change stateID bit with the same index as in the vector
            stateID[index] = 1;
        }

        /* Trim the size of the array which is returned */
        // Find the last 1 in the stateID
        for (int i = stateID.length-1; i > -1; i--) {
            if (stateID[i] == 1) {
                index = i;
                break;
            }
        }

        // Copy the relevant part of the bit string to the return array
        int[] trimmed_stateID = new int[index+1];
        System.arraycopy(stateID, 0, trimmed_stateID, 0, index+1);

        return trimmed_stateID;
    }

    /*
     * @return an integer from the stateID integer array
     */
    public int stateIDToInt(int[] stateID)
    {
        int integerStateID = 0;

        for (int i : stateID) {
            integerStateID *= 10;
            integerStateID += i;
        }

        return integerStateID;
    }

    /*
     * @return an integer, in reverse order, from the stateID integer array
     */
    public int stateIDToReverseInt(int[] stateID)
    {
        int integerStateID = 0;

        for (int i = stateID.length-1; i > 0; i--) {
            integerStateID += stateID[i];
            integerStateID *= 10;
        }

        return integerStateID;
    }
}
