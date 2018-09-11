/**
 * Natural Language Parse program by Napier Software in 2015
 *
 * Noun.java
 *
 */

/*import Word;
import Part_of_speech;
import Number;
*/
/**
 *
 * @author  Chris Napier
 * @version 1.0
 */
public class Noun extends Word {


    /** Create new Noun with given spelling and number */
    public Noun(String spell, int num) {
        super(Part_of_speech.NOUN, spell, num, Person.NONE);
        
    }
    
    

}
