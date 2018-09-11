/**
 * Natural Language Parse program by Napier Software in 2015
 *
 * Preposition.java
 *
 */
/*
import Word;
import Part_of_speech;
import Number;
import Person;
*/
/**
 *
 * @author   Chris Napier
 * @version 1.0
 */
public class Preposition extends Word {

    /** Create new given Preposition */
    public Preposition(String spell) {
        super(Part_of_speech.PREPOSITION, spell, Number.NONE, Person.NONE);
    }
}
