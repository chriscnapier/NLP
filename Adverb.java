/**
 * Natural Language Parse program by Napier Software in 2015
 *
 * Adverb.java
 *
 */


/*import Word;
import Part_of_speech;
import Number;
import Person;
*/

/**
 *
 * @author  Chris Napier
 * @version 1.0
 */
public class Adverb extends Word {

    /** Create new Adverb with given spelling */
    public Adverb(String spell) {
        super(Part_of_speech.ADVERB, spell, Number.NONE, Person.NONE);
    }

}
