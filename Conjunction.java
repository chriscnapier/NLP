/**
 * Natural Language Parse program by Napier Software in 2015
 *
 * Conjunction.java
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
public class Conjunction extends Word {

    /** Create new Conjunction with given spelling */
    public Conjunction(String spell) {
        super(Part_of_speech.CONJUNCTION, spell, Number.NONE, Person.NONE);
    }

}
