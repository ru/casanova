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
    public String[] pullState(int[] hashKey)
    {
        int index = 0;
        String tempString[] = new String[hashKey.length];
        
        // Find the compound sentences in the vector
        for(int i=0; i<hashKey.length; i++) {
            if (hashKey[i] == 1) {
                tempString[index] = m_compoundVector.get(i);
                index++;
            }
        }

        // Create a string array to return
        String[] stringToReturn = new String[index];
        System.arraycopy(tempString, 0, stringToReturn, 0, index);

        return stringToReturn;
    }

                                                                                                                  
    /*
     * Creates a StateID (bit-string in an integer array) when given the compound statements a state.
     */
    public int[] pushState(String compoundStatements[])
    {
        int index = 0;
        int[] stateID = new int[compoundStatements.length + m_compoundVector.size()];

        /* Create a state array */
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

        
        /* Trim the size of the array which is returned! */
        // Find the last 1 in the stateID
        for (int i = stateID.length-1; i > -1; i--) {
            if (stateID[i] == 1) {
                index = i;
                break;
            }
        }

        // Copy the relevant part of the bit string to the return array
        int[] stateToReturn = new int[index+1];
        System.arraycopy(stateID, 0, stateToReturn, 0, index+1);

        return stateToReturn;
    }
}