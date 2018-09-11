/**
 * Natural Language Parse program by Napier Software in 2015
 *
 * Proper_noun.java
 *
 */

/*import Word;
import Part_of_speech;
import Number;
import Person;
*/
/**
 *
 * @author   Chris Napier
 * @version 1.0
 */
public class Proper_noun extends Word {

    /** Create new Proper_noun with given spelling */
    public Proper_noun(String spell) {
        super(Part_of_speech.PROPER_NOUN, spell, Number.S, Person.NONE);
    }

}
