package de.tudresden.inf.ggp.basicplayer;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: ellioman
 * Date: Mar 18, 2010
 * Time: 9:16:54 PM
 */
public class StateHash
{
    Hashtable m_compoundHashTable = new Hashtable();
    ArrayList<String> m_compoundVector = new ArrayList<String>();

    /*
     * Constructor
     */
    public StateHash() { }

    
    /*
     * Returns a string which represents a state from the hash key (bit-string in an integer array) from the input.
     */
    public String pullState(int[] hashKey)
    {
        String returnString = "";

        for(int i=0; i<hashKey.length; i++)
        {
            if (hashKey[i] == 1) {
                returnString += m_compoundVector.get(i) + " ";
            }
        }

        return returnString;
    }

                                                                                                                  
    /*
     * Creates a StateID (bit-string in an integer array) when given the compound statements a state.
     */
    public int[] pushState(String compoundStatements[])
    {
        int index;
        int[] stateID = new int[compoundStatements.length + m_compoundVector.size()];

        // For each string in the array...
        for (String c : compoundStatements) {

            // If the compound is already in the vector..
            if (m_compoundHashTable.containsKey(c.hashCode())) {
                index = (Integer)m_compoundHashTable.get(c.hashCode());
            }

            // If the compound is not in the vector...
            else {
                m_compoundVector.add(c);
                index = m_compoundVector.indexOf(c);
                m_compoundHashTable.put(c.hashCode(), index);
            }

            // Change stateID bit with the same index as in the vector
            stateID[index] = 1;
        }

        // TODO: Trim the size of the array which is returned!

        return stateID;
    }
}