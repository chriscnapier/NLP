/**
 * Natural Language Parse program by Napier Software in 2015
 * 
 * Verb.java
 *
 */

/*import Word;
import Part_of_speech;
import Tense;
*/
/**
 *
 * @author   Chris Napier
 * @version 1.0
 */
public class Verb extends Word {

    private boolean transitive = true;
    private int tense = Tense.PRESENT;

    /** Create new given Verb */
    /*
    public Verb(String spell) {
        super(Part_of_speech.VERB, spell, Number.NONE, Person.NONE);
    }
    */
    
    /** Create new given Verb */
    public Verb(String spell, int tense_in, int person_in) {
        super(Part_of_speech.VERB, spell, Number.NONE, person_in);
        tense = tense_in;
    }
    
    //Get and put
    public int gettense() { return tense;}
    public void puttense(int tense_in) {tense = tense_in;}
    
    /** Return the attributes of a Verb object in text form */
    public String toString() {
        String str = super.toString();

        if (tense != Tense.NONE)
            str += " Tense: " + Tense.toString(tense);
       
        return str;
    }
}
