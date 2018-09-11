/**
 * Natural Language Parse program by Napier Software in 2015
 *
 * Determiner.java
 *
 */

/*import Word;
import Part_of_speech;
import Person;
*/

/**
 *
 * @author  Chris Napier
 * @version 1.0
 */
public class Determiner extends Word {

    /** Create new given Determiner */
    public Determiner(String spelling, int num) {
        super(Part_of_speech.DETERMINER, spelling, num, Person.NONE);
        
    }
}
