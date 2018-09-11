/**
 * Natural Language Parse program by Napier Software in 2015
 *
 * Adjective.java
 *
 */


/**
 *
 * @author  Chris Napier
 * @version 1.0
 */
public class Adjective extends Word {

    /** Create new Adjective with given spelling */
    public Adjective(String spell) {
        super(Part_of_speech.ADJECTIVE, spell, Number.NONE, Person.NONE);
    }

}
